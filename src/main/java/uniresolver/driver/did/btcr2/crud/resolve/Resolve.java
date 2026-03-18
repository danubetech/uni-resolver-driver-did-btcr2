package uniresolver.driver.did.btcr2.crud.resolve;

import com.danubetech.dataintegrity.jsonld.DataIntegrityKeywords;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.Service;
import foundation.identity.did.representations.Representations;
import foundation.identity.did.validation.Validation;
import foundation.identity.jsonld.JsonLDUtils;
import fr.acinq.bitcoin.BlockHash;
import fr.acinq.bitcoin.PublicKey;
import io.ipfs.multibase.binary.Base64;
import io.leonard.Base58;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.AddressParser;
import org.bitcoinj.uri.BitcoinURI;
import org.bitcoinj.uri.BitcoinURIParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btcr2.Network;
import uniresolver.driver.did.btcr2.algorithms.JSONDocumentHashing;
import uniresolver.driver.did.btcr2.algorithms.SMTProofVerification;
import uniresolver.driver.did.btcr2.beacons.CASBeacon;
import uniresolver.driver.did.btcr2.beacons.SMTBeacon;
import uniresolver.driver.did.btcr2.beacons.SingletonBeacon;
import uniresolver.driver.did.btcr2.connections.bitcoin.BitcoinConnection;
import uniresolver.driver.did.btcr2.connections.bitcoin.BitcoinConnector;
import uniresolver.driver.did.btcr2.connections.bitcoin.records.Block;
import uniresolver.driver.did.btcr2.connections.bitcoin.records.Tx;
import uniresolver.driver.did.btcr2.connections.ipfs.IPFSConnection;
import uniresolver.driver.did.btcr2.data.json.CASAnnouncement;
import uniresolver.driver.did.btcr2.data.json.SMTProof;
import uniresolver.driver.did.btcr2.data.json.SidecarData;
import uniresolver.driver.did.btcr2.data.jsonld.BTCR2Update;
import uniresolver.driver.did.btcr2.data.records.GenesisBytesType;
import uniresolver.driver.did.btcr2.data.records.IdentifierComponents;
import uniresolver.driver.did.btcr2.syntax.DidBtcr2IdentifierDecoding;
import uniresolver.driver.did.btcr2.util.JSONPatchUtil;
import uniresolver.result.ResolveResult;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Resolve {

    private static final String INITIAL_DID_DOCUMENT_TEMPLATE =
            """
                {
                  "@context": [
                    "https://www.w3.org/TR/did-1.1",
                    "https://btcr2.dev/context/v1"
                  ],
                  "id": "{{did}}",
                  "controller": [
                    "{{did}}"
                  ],
                  "verificationMethod": [
                    {
                      "id": "{{did}}#initialKey",
                      "type": "Multikey",
                      "controller": "{{did}}",
                      "publicKeyMultibase": "{{public-key-multikey}}"
                    }
                  ],
                  "authentication": [
                    "{{did}}#initialKey"
                  ],
                  "assertionMethod": [
                    "{{did}}#initialKey"
                  ],
                  "capabilityInvocation": [
                    "{{did}}#initialKey"
                  ],
                  "capabilityDelegation": [
                    "{{did}}#initialKey"
                  ],
                  "service": [
                    {
                      "id": "{{did}}#initialP2PKH",
                      "type": "SingletonBeacon",
                      "serviceEndpoint": "{{p2pkh-bitcoin-address}}"
                    },
                    {
                      "id": "{{did}}#initialP2WPKH",
                      "type": "SingletonBeacon",
                      "serviceEndpoint": "{{p2wpkh-bitcoin-address}}"
                    },
                    {
                      "id": "{{did}}#initialP2TR",
                      "type": "SingletonBeacon",
                      "serviceEndpoint": "{{p2tr-bitcoin-address}}"
                    }
                  ]
                }
            """;
    private static final Pattern PATTERN_TX_SIGNALBYTES = Pattern.compile("^OP_RETURN OP_PUSHBYTES_32 ([0-9a-fA-F]{64})$");

    private static final Logger log = LoggerFactory.getLogger(Resolve.class);

    private static final JsonMapper jsonMapper = JsonMapper.builder()
            .configure(MapperFeature.USE_GETTERS_AS_SETTERS, false)
            .build();

    private BitcoinConnector bitcoinConnector;
    private IPFSConnection ipfsConnection;

    public Resolve(BitcoinConnector bitcoinConnector, IPFSConnection ipfsConnection) {
        this.bitcoinConnector = bitcoinConnector;
        this.ipfsConnection = ipfsConnection;
    }

    /*
     * Resolve
     * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#resolve
     */

    public ResolveResult resolve(DID identifier, Map<String, Object> resolutionOptions) throws ResolutionException {

        // Resolution maintains the following state while building the DID document:

        List<Map.Entry<Block, BTCR2Update>> updates = new ArrayList<>();
        DIDDocument current_document;
        int current_version_id = 1;
        List<byte[]> update_hash_history = new ArrayList<>();
        Integer block_confirmations = null;

        /*
         * Decode the DID
         * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#decode-the-did
         */

        // The did MUST be parsed with the DID-BTCR2 Identifier Decoding algorithm to retrieve version, network, and genesis_bytes.

        IdentifierComponents identifierComponents = DidBtcr2IdentifierDecoding.didBtcr2IdentifierDecoding(identifier);

        /*
         * Process Sidecar Data
         * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#process-sidecar-data
         */

        // resolutionOptions contains a sidecar property (Sidecar Data (data structure)) which SHOULD be prepared as lookup tables:

        Map<String, Object> sidecarMap = resolutionOptions == null ? null : (Map<String, Object>) resolutionOptions.get("sidecar");
        SidecarData sidecar = sidecarMap == null ? null : jsonMapper.convertValue(sidecarMap, SidecarData.class);

        // Hash each BTCR2 Signed Update (data structure) in sidecar.updates with the JSON Document Hashing algorithm
        // and build a map from hash to update (update_lookup_table).

        List<BTCR2Update> sidecarUpdates = sidecar == null ? null : sidecar.getUpdates();
        Map<byte[], BTCR2Update> update_lookup_table = sidecarUpdates == null ? null : sidecarUpdates.stream().collect(Collectors.toMap(JSONDocumentHashing::jsonDocumentHashing, btcr2Update -> btcr2Update));

        // Hash each CAS Announcement (data structure) in sidecar.casUpdates with the JSON Document Hashing algorithm
        // and build a map from hash to announcement (cas_lookup_table).

        List<CASAnnouncement> sidecarCasUpdates = sidecar == null ? null : sidecar.getCasUpdates();
        Map<byte[], CASAnnouncement> cas_lookup_table = sidecarCasUpdates == null ? null : sidecarCasUpdates.stream().collect(Collectors.toMap(JSONDocumentHashing::jsonDocumentHashing, casAnnouncement -> casAnnouncement));

        // Build a map from sidecar.smtProofs keyed by proof id (smt_lookup_table).

        List<SMTProof> smtProofs = sidecar == null ? null : sidecar.getSmtProofs();
        Map<byte[], SMTProof> smt_lookup_table = smtProofs == null ? null : smtProofs.stream().collect(Collectors.toMap(smtProof -> Base64.decodeBase64(smtProof.getId()), smtProof -> smtProof));

        // If genesis_bytes is a SHA-256 hash, hash sidecar.genesisDocument with the JSON Document Hashing algorithm.
        // Raise an INVALID_DID error if the computed hash does not match genesis_bytes.

        if (GenesisBytesType.SHA256HASH == identifierComponents.genesisBytesType()) {
            DIDDocument genesisDocument = sidecar == null ? null : sidecar.getGenesisDocument();
            if (genesisDocument != null) {
                byte[] genesisDocumentHash = JSONDocumentHashing.jsonDocumentHashing(genesisDocument);
                if (! Arrays.equals(genesisDocumentHash, identifierComponents.genesisBytes())) {
                    throw new ResolutionException(ResolutionException.ERROR_INVALID_DID, "Computed hash " + Base64.encodeBase64String(genesisDocumentHash) + " does not match genesis_bytes " + Base64.encodeBase64String(identifierComponents.genesisBytes()));
                }
            }
        }

        // The resolver: 1. Establishes current_document from the DID or from Sidecar Data.

        /*
         * Establish current_document
         * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#establish-current-document
         */

        // Choose how to establish current_document based on the type of genesis_bytes retrieved from the decoded did:

        current_document = switch (identifierComponents.genesisBytesType()) {

            /*
             * If genesis_bytes is a SHA-256 Hash
             * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#if-genesis_bytes-is-a-sha-256-hash
             */

            case SHA256HASH -> {

                // Process the Genesis Document provided in sidecar.genesisDocument by replacing the identifier
                // placeholder ("did:btcr2:_") with the did.

                DIDDocument genesisDocument = sidecar == null ? null : sidecar.getGenesisDocument();
                if (genesisDocument == null) throw new ResolutionException(ResolutionException.ERROR_INVALID_OPTIONS, "Missing genesis document in sidecar data");
                yield DIDDocument.fromJson(sidecar.getGenesisDocument().toJson().replace("did:btcr2:_", identifier.getDidString()));
            }

            /*
             * If genesis_bytes is a secp256k1 Public Key
             * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#if-genesis_bytes-is-a-secp256k1-public-key
             */

            case SECP256K1PUBLICKEY -> {

                // Render the Initial DID Document template with these values
                // (Bitcoin addresses MUST use the Bitcoin URI Scheme [BIP321]):

                byte[] publicKeyBytes = identifierComponents.genesisBytes();
                AddressParser addressParser = AddressParser.getDefault();
                Network network = identifierComponents.network();
                PublicKey initialPublicKey = PublicKey.parse(publicKeyBytes);

                String did = identifier.getDidString();
                String public_key_multikey = Base58.encode(publicKeyBytes);
                String p2pkh_bitcoin_address = BitcoinURI.convertToBitcoinURI(network.toBitcoinjNetwork(), addressParser.parseAddress(initialPublicKey.p2pkhAddress(new BlockHash(this.getBitcoinConnector().getGensisHash(network)))).toString(), null, null, null);
                String p2wpkh_bitcoin_address = BitcoinURI.convertToBitcoinURI(network.toBitcoinjNetwork(), addressParser.parseAddress(initialPublicKey.p2wpkhAddress(new BlockHash(this.getBitcoinConnector().getGensisHash(network)))).toString(), null, null, null);
                String p2tr_bitcoin_address = BitcoinURI.convertToBitcoinURI(network.toBitcoinjNetwork(), addressParser.parseAddress(initialPublicKey.p2trAddress(new BlockHash(this.getBitcoinConnector().getGensisHash(network)))).toString(), null, null, null);

                String initialDidDocumentString = INITIAL_DID_DOCUMENT_TEMPLATE
                        .replace("{{did}}", did)
                        .replace("{{public-key-multikey}}", public_key_multikey)
                        .replace("{{p2pkh-bitcoin-address}}", p2pkh_bitcoin_address)
                        .replace("{{p2wpkh-bitcoin-address}}", p2wpkh_bitcoin_address)
                        .replace("{{p2tr-bitcoin-address}}", p2tr_bitcoin_address);

                // Parse the rendered template as JSON to form current_document.

                yield DIDDocument.fromJson(initialDidDocumentString);
            }
        };

        // 2. Repeats the following loop:

        process: while (true) {

            /*
             * Process Beacon Signals
             * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#process-beacon-signals
             */

            // Scan the service entries in current_document (DID Document (data structure))
            // and identify BTCR2 Beacons by matching service type to Beacons Table 1: Beacon Types.

            List<Service> beaconServices = current_document.getServices();
            if (beaconServices == null) {
                if (log.isWarnEnabled()) log.warn("No services found in current_document: {}", current_document);
            } else {
                beaconServices = beaconServices.stream().filter(service -> Arrays.asList(SingletonBeacon.TYPE, CASBeacon.TYPE, SMTBeacon.TYPE).contains(service.getType())).toList();
                if (beaconServices.isEmpty()) {
                    if (log.isWarnEnabled()) log.warn("No beacon services found in current_document: {}", current_document);
                }
            }

            // Parse each beacon serviceEndpoint as a Beacon Address

            Map<Address, String> beaconsAddresses = new LinkedHashMap<>();
            if (beaconServices != null) {
                for (Service beaconService : beaconServices) {
                    try {
                        Address beaconAddress = BitcoinURI.of((beaconService.getServiceEndpoint()).toString()).getAddress();
                        String beaconServiceType = beaconService.getType();
                        if (log.isDebugEnabled()) log.debug("Adding beacon address {} for service type {}", beaconAddress, beaconServiceType);
                        beaconsAddresses.put(beaconAddress, beaconServiceType);
                    } catch (BitcoinURIParseException ex) {
                        throw new ResolutionException(ResolutionException.ERROR_INVALID_DID_DOCUMENT, "Cannot parse Bitcoin address: " + beaconService.getServiceEndpoint());
                    }
                }
            }

            // then use those Beacon Addresses to find Bitcoin transactions whose last output script contains Signal Bytes.

            Map<Tx, Block> beaconsBlocks = new LinkedHashMap<>();
            Map<Tx, String> beaconsServiceTypes = new LinkedHashMap<>();
            Map<Tx, byte[]> beaconsSignalBytes = new LinkedHashMap<>();

            BitcoinConnection bitcoinConnection = this.getBitcoinConnector().getBitcoinConnection(identifierComponents.network());
            for (Map.Entry<Address, String> beaconAddressEntry : beaconsAddresses.entrySet()) {
                Address beaconAddress = beaconAddressEntry.getKey();
                String beaconServiceType = beaconAddressEntry.getValue();
                for (Tx beaconTransaction : bitcoinConnection.getAddressTransactions(beaconAddress)) {
                    Block beaconBlock = bitcoinConnection.getBlockByTransaction(beaconTransaction);
                    Matcher matcher = PATTERN_TX_SIGNALBYTES.matcher(beaconTransaction.txOuts().getLast().asm());
                    if (! matcher.matches()) {
                        if (log.isDebugEnabled()) log.debug("Transaction {} does not have signal bytes. Skipping.", beaconTransaction);
                        continue;
                    }
                    String beaconSignalBytesString = matcher.group(1);
                    byte[] beaconSignalBytes;
                    try {
                        beaconSignalBytes = Hex.decodeHex(beaconSignalBytesString);
                    } catch (DecoderException ex) {
                        if (log.isWarnEnabled()) log.warn("Transaction {} has invalid signal bytes: {}. Skipping.", beaconTransaction, beaconSignalBytesString);
                        continue;
                    }
                    if (log.isDebugEnabled()) log.debug("Transaction {} has block {} and service type {} and signal bytes {}. Adding.", beaconTransaction, beaconBlock, beaconServiceType, beaconSignalBytesString);
                    beaconsBlocks.put(beaconTransaction, beaconBlock);
                    beaconsServiceTypes.put(beaconTransaction, beaconServiceType);
                    beaconsSignalBytes.put(beaconTransaction, beaconSignalBytes);
                }
            }

            // For each transaction found:

            for (Tx beaconTransaction : beaconsServiceTypes.keySet()) {

                Block beaconBlock = beaconsBlocks.get(beaconTransaction);
                String beaconServiceType = beaconsServiceTypes.get(beaconTransaction);
                byte[] beaconSignalBytes = beaconsSignalBytes.get(beaconTransaction);

                // Derive update_hash from the transaction’s Signal Bytes based on the beacon type:

                byte[] update_hash = switch (beaconServiceType) {

                    case SingletonBeacon.TYPE ->
                            // update_hash is the Signal Bytes.
                            beaconSignalBytes;

                    case CASBeacon.TYPE ->
                            // use Process CAS Beacon.
                            processCASBeacon(beaconSignalBytes, identifier, cas_lookup_table);

                    case SMTBeacon.TYPE ->
                            // use Process SMT Beacon.
                            processSMTBeacon(beaconSignalBytes, smt_lookup_table);

                    default -> null;
                };

                if (update_hash == null) {
                    if (log.isWarnEnabled()) log.warn("Transaction {} with service type {} has no update_hash. Skipping.", beaconTransaction, beaconServiceType);
                    continue;
                }

                // Build a tuple with: The transaction’s block metadata (height, time, and confirmations).
                // The BTCR2 Signed Update (data structure) retrieved from update_lookup_table[update_hash].
                // If the update is not in update_lookup_table, raise a MISSING_UPDATE_DATA error.

                if (update_lookup_table == null) throw new ResolutionException(ResolutionException.ERROR_INVALID_OPTIONS, "No update_lookup_table provided");

                BTCR2Update update = update_lookup_table.get(update_hash);
                if (update == null) throw new ResolutionException("MISSING_UPDATE_DATA", "No update found for update_hash " + Base64.encodeBase64String(update_hash));

                Map.Entry<Block, BTCR2Update> updateTuple = Map.entry(beaconBlock, update);

                // Append the tuple to updates.

                updates.add(updateTuple);
            }

            /*
             * Process updates Array
             * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#process-updates
             */

            // Resolve current_document as didDocument if updates is empty.

            if (updates.isEmpty()) {
                if (log.isDebugEnabled()) log.debug("No updates. Returning current_document.");
                break process;
            }

            // 1. Sort updates by BTCR2 Signed Update (data structure) targetVersionId (ascending)
            // with the tuple’s block height as a tiebreaker.

            updates.sort(Comparator
                    .comparingInt((Map.Entry<Block, BTCR2Update> a) -> a.getValue().getTargetVersionId())
                    .thenComparingInt(a -> a.getKey().blockHeight()));

            // Take the first tuple.

            tuples: for (Map.Entry<Block, BTCR2Update> tuple : updates) {

                // 2. Set block_confirmations to the tuple’s block confirmations.

                block_confirmations = tuple.getKey().confirmations();

                // 3. If resolutionOptions.versionTime is provided and the tuple’s block time is more recent,
                // resolve current_document as didDocument.

                if (resolutionOptions.containsKey("versionTime")) {
                    long versionTime = (long) resolutionOptions.get("versionTime");
                    if (tuple.getKey().blockTime() > versionTime) {
                        if (log.isDebugEnabled()) log.debug("Block time {} is more recent than resolutionOptions.versionTime {}. Returning current_document.", tuple.getKey().blockTime(), versionTime);
                        break process;
                    }
                }

                // 4. Set update to the tuple’s BTCR2 Signed Update (data structure) and check update.targetVersionId.

                BTCR2Update update = tuple.getValue();
                current_document = checkUpdateTargetVersionId(current_document, update, identifier, current_version_id, update_hash_history);

                // 5. Increment current_version_id.

                current_version_id++;

                // 6. If current_version_id is greater than or equal to the integer form of resolutionOptions.versionId,
                // resolve current_document as didDocument.

                if (resolutionOptions.containsKey("versionId")) {
                    long versionId = (long) resolutionOptions.get("versionId");
                    if (current_version_id >= versionId) {
                        if (log.isDebugEnabled()) log.debug("current_version_id {} is greater than or equal to resolutionOptions.versionId {}. Returning current_document.", current_version_id, versionId);
                        break process;
                    }
                }

                // 7. If current_document.deactivated is true, resolve current_document as didDocument.

                Boolean deactivated = null;
                if (current_document.getJsonObject().containsKey("deactivated")) deactivated = ((Boolean) current_document.getJsonObject().get("deactivated"));
                if (Boolean.TRUE.equals(deactivated)) {
                    if (log.isDebugEnabled()) log.debug("current_document.deactivated is true. Returning current_document.");
                    break process;
                }
            }
        }

        // done

        Map<String, Object> didResolutionMetadata = new LinkedHashMap<>();
        didResolutionMetadata.put("contentType", Representations.DEFAULT_MEDIA_TYPE);
        Map<String, Object> didDocumentMetadata = new LinkedHashMap<>();
        didDocumentMetadata.put("versionId", Integer.toString(current_version_id));
        didDocumentMetadata.put("confirmations", (block_confirmations == null ? null : block_confirmations.toString()));
        didDocumentMetadata.put("deactivated", current_document.getJsonObject().get("deactivated"));
        didDocumentMetadata.put("identifierComponents", Map.of(
                "version", identifierComponents.version(),
                "network", identifierComponents.network().toString(),
                "genesisBytes", Hex.encodeHexString(identifierComponents.genesisBytes()),
                "genesisBytesTypes", identifierComponents.genesisBytesType()));

        ResolveResult resolveResult = ResolveResult.build(didResolutionMetadata, current_document, didDocumentMetadata);
        if (log.isDebugEnabled()) log.debug("resolveResult: {}", resolveResult);
        return resolveResult;
    }

    /*
     * Process CAS Beacon
     * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#process-cas-beacon
     */
    private static byte[] processCASBeacon(byte[] signalBytes, DID did, Map<byte[], CASAnnouncement> cas_lookup_table) throws ResolutionException {

        if (cas_lookup_table == null) throw new ResolutionException(ResolutionException.ERROR_INVALID_OPTIONS, "No cas_lookup_table provided");

        // Treat Signal Bytes as map_update_hash.

        byte[] map_update_hash = signalBytes;

        // Look up map_update_hash in cas_lookup_table to retrieve a CAS Announcement (data structure)

        CASAnnouncement casAnnouncement = cas_lookup_table.get(map_update_hash);
        if (casAnnouncement == null) throw new ResolutionException(ResolutionException.ERROR_INVALID_DID_DOCUMENT, "No CAS Announcement found for map_update_hash " + Base64.encodeBase64String(map_update_hash));

        // and read update_hash from the announcement entry keyed by did.

        String update_hash_string = casAnnouncement.get(did.getDidString());
        if (update_hash_string == null) throw new ResolutionException(ResolutionException.ERROR_INVALID_DID_DOCUMENT, "No update_hash found for DID " + did + " and map_update_hash " + Base64.encodeBase64String(map_update_hash));

        // done

        byte[] update_hash = Base64.decodeBase64(update_hash_string);
        if (log.isDebugEnabled()) log.debug("For did {} and map_update_hash {} found update_hash: {}", did, Base64.encodeBase64String(map_update_hash), Base64.encodeBase64String(update_hash));
        return update_hash;
    }

    /*
     * Process SMT Beacon
     * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#process-smt-beacon
     */
    private static byte[] processSMTBeacon(byte[] signalBytes, Map<byte[], SMTProof> smt_lookup_table) throws ResolutionException {

        if (smt_lookup_table == null) throw new ResolutionException(ResolutionException.ERROR_INVALID_OPTIONS, "No smt_lookup_table provided");

        // Treat Signal Bytes as smt_root.

        byte[] smt_root = signalBytes;

        // Look up smt_root in smt_lookup_table to retrieve an SMT Proof (data structure).

        SMTProof smtProof = smt_lookup_table.get(smt_root);
        if (smtProof == null) throw new ResolutionException(ResolutionException.ERROR_INVALID_DID_DOCUMENT, "No SMT Proof found for smt_root " + Base64.encodeBase64String(smt_root));

        // Validate the proof with the SMT Proof Verification algorithm.

        SMTProofVerification.smtProofVerification(smtProof);

        // Use smt_proof.updateId as update_hash.

        byte[] update_hash = Base64.decodeBase64(smtProof.getUpdateId());
        if (log.isDebugEnabled()) log.debug("For smt_root {} found update_hash: {}", Base64.encodeBase64String(smt_root), Base64.encodeBase64String(update_hash));
        return update_hash;
    }

    /*
     * Check update.targetVersionId
     * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#check-update-version
     */
    private static DIDDocument checkUpdateTargetVersionId(DIDDocument current_document, BTCR2Update update, DID did, int current_version_id, List<byte[]> update_hash_history) throws ResolutionException {

        // Compare update.targetVersionId to current_version_id.

        // update.targetVersionId <= current_version_id:
        // Confirm Duplicate Update.

        if (update.getTargetVersionId() <= current_version_id) {
            confirmDuplicateUpdate(update, update_hash_history);
            return current_document;
        }

        // update.targetVersionId == current_version_id + 1:
        // Apply update.

        if (update.getTargetVersionId() == current_version_id + 1) {
            return applyUpdate(current_document, update, did, update_hash_history);
        }

        // update.targetVersionId > current_version_id + 1:
        // LATE_PUBLISHING error MUST be raised.

        if (update.getTargetVersionId() > current_version_id + 1) {
            throw new ResolutionException("LATE_PUBLISHING", "update.targetVersionId (" + update.getTargetVersionId() + ") is greater than current_version_id + 1 (" + (current_version_id + 1) + ")");
        }

        // done

        return current_document;
    }

    /*
     * Confirm Duplicate Update
     * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#confirm-duplicate-update
     */
    private static void confirmDuplicateUpdate(BTCR2Update update, List<byte[]> update_hash_history) throws ResolutionException {

        // Create unsigned_update by removing the proof property from update.

        BTCR2Update unsigned_update = BTCR2Update.fromJson(update.toJson());
        JsonLDUtils.jsonLdRemove(unsigned_update, DataIntegrityKeywords.JSONLD_TERM_PROOF);

        // Hash unsigned_update with the JSON Document Hashing algorithm

        byte[] unsignedUpdateHash = JSONDocumentHashing.jsonDocumentHashing(unsigned_update);

        // and compare it to update_hash_history[update.targetVersionId - 2].
        // Raise a LATE_PUBLISHING error if the hashes differ.

        if (! Arrays.equals(unsignedUpdateHash, update_hash_history.get(update.getTargetVersionId() - 2))) {
            throw new ResolutionException("LATE_PUBLISHING", "unsigned_update hash (" + Hex.encodeHexString(unsignedUpdateHash) + ") differs from update_hash_history[" + (update.getTargetVersionId()-2) + "] (" + Hex.encodeHexString(update_hash_history.get(update.getTargetVersionId() - 2)) + ")");
        }
    }

    /*
     * Apply update
     * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#apply-update
     */
    private static DIDDocument applyUpdate(DIDDocument current_document, BTCR2Update update, DID did, List<byte[]> update_hash_history) throws ResolutionException {

        // Hash current_document with the JSON Document Hashing algorithm.

        byte[] currentDocumentHash = JSONDocumentHashing.jsonDocumentHashing(current_document);

        // Raise an INVALID_DID_UPDATE error if the result does not match the decoded update.sourceHash.

        if (! Arrays.equals(currentDocumentHash, Base64.decodeBase64(update.getSourceHash()))) {
            throw new ResolutionException("INVALID_DID_UPDATE", "current_document hash (" + Base64.encodeBase64String(currentDocumentHash) + ") differs from update.sourceHash (" + update.getSourceHash() + ")");
        }

        // Check update.proof.

        checkUpdateProof();

        // Apply the update.patch JSON Patch [RFC6902] to current_document.

        current_document = JSONPatchUtil.apply(current_document, update.getPatch());

        // Verify that current_document conforms to DID Core v1.1 [DID-CORE]
        // and that current_document.id equals did. Otherwise raise INVALID_DID_UPDATE.

        try {
            Validation.validate(current_document);
        } catch (Exception ex) {
            throw new ResolutionException("INVALID_DID_UPDATE", "current_document is not valid: " + ex.getMessage(), ex);
        }
        if (! current_document.getId().equals(did.toUri())) {
            throw new ResolutionException("INVALID_DID_UPDATE", "current_document.id " + current_document.getId() + " does not equal did " + did);
        }

        // Hash the patched current_document with the JSON Document Hashing algorithm.

        currentDocumentHash = JSONDocumentHashing.jsonDocumentHashing(current_document);

        // Raise an INVALID_DID_UPDATE error if the result does not match the decoded update.targetHash.

        if (! Arrays.equals(currentDocumentHash, Base64.decodeBase64(update.getTargetHash()))) {
            throw new ResolutionException("INVALID_DID_UPDATE", "current_document hash (" + Base64.encodeBase64String(currentDocumentHash) + ") differs from update.targetHash (" + update.getTargetHash() + ")");
        }

        // Create unsigned_update by removing the proof property from update

        BTCR2Update unsigned_update = BTCR2Update.fromJson(update.toJson());
        JsonLDUtils.jsonLdRemove(unsigned_update, DataIntegrityKeywords.JSONLD_TERM_PROOF);

        // hash it with the JSON Document Hashing algorithm

        byte[] unsignedUpdateHash = JSONDocumentHashing.jsonDocumentHashing(unsigned_update);

        // append the hash to update_hash_history.

        update_hash_history.add(unsignedUpdateHash);

        // done

        return current_document;
    }

    /*
     * Check update.proof
     * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#check-update-proof
     */
    private static void checkUpdateProof() {

        // TODO
    }

    /*
     * Getters and setters
     */

    public BitcoinConnector getBitcoinConnector() {
        return bitcoinConnector;
    }

    public void setBitcoinConnector(BitcoinConnector bitcoinConnector) {
        this.bitcoinConnector = bitcoinConnector;
    }

    public IPFSConnection getIpfsConnection() {
        return ipfsConnection;
    }

    public void setIpfsConnection(IPFSConnection ipfsConnection) {
        this.ipfsConnection = ipfsConnection;
    }
}
