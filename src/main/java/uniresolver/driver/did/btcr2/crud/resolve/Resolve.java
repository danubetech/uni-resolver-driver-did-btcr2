package uniresolver.driver.did.btcr2.crud.resolve;

import com.apicatalog.multicodec.MulticodecDecoder;
import com.danubetech.btc.connection.BitcoinConnection;
import com.danubetech.btc.connection.BitcoinConnector;
import com.danubetech.btc.connection.Network;
import com.danubetech.btc.connection.records.Block;
import com.danubetech.btc.connection.records.Tx;
import com.danubetech.btc.syntax.GenesisBytesType;
import com.danubetech.btc.syntax.IdentifierComponents;
import com.danubetech.btc.util.AddressUtil;
import com.danubetech.dataintegrity.DataIntegrityProof;
import com.danubetech.dataintegrity.jsonld.DataIntegrityKeywords;
import com.danubetech.dataintegrity.verifier.DataIntegrityProofLdVerifier;
import com.danubetech.dataintegrity.verifier.LdVerifierRegistry;
import com.danubetech.keyformats.crypto.impl.secp256k1_ES256KS_PublicKeyVerifier;
import foundation.identity.did.*;
import foundation.identity.did.representations.Representations;
import foundation.identity.did.validation.Validation;
import foundation.identity.jsonld.JsonLDDereferencer;
import foundation.identity.jsonld.JsonLDException;
import foundation.identity.jsonld.JsonLDObject;
import foundation.identity.jsonld.JsonLDUtils;
import fr.acinq.bitcoin.BlockHash;
import fr.acinq.bitcoin.PublicKey;
import io.ipfs.cid.Cid;
import io.ipfs.multibase.Multibase;
import io.ipfs.multihash.Multihash;
import jakarta.json.Json;
import jakarta.json.JsonPatch;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.base.AddressParser;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.uri.BitcoinURI;
import org.bitcoinj.uri.BitcoinURIParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btcr2.algorithms.JSONDocumentHashing;
import uniresolver.driver.did.btcr2.algorithms.SMTProofVerification;
import uniresolver.driver.did.btcr2.appendix.RootDidBtcr2UpdateCapabilities;
import uniresolver.driver.did.btcr2.beacons.BeaconType;
import uniresolver.driver.did.btcr2.data.CASAnnouncement;
import uniresolver.driver.did.btcr2.data.SMTProof;
import uniresolver.driver.did.btcr2.data.SidecarData;
import uniresolver.driver.did.btcr2.data.jsonld.BTCR2Update;
import uniresolver.driver.did.btcr2.data.jsonld.RootCapability;
import uniresolver.driver.did.btcr2.ipfs.IPFSConnection;
import uniresolver.driver.did.btcr2.syntax.DidBtcr2IdentifierDecoding;
import uniresolver.driver.did.btcr2.util.BytesArray;
import uniresolver.driver.did.btcr2.util.JSONPatchUtil;
import uniresolver.result.ResolveResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Resolve {

    private static final String INITIAL_DID_DOCUMENT_TEMPLATE =
            """
                {
                  "@context": [
                    "https://www.w3.org/ns/did/v1.1",
                    "https://btcr2.dev/context/v1"
                  ],
                  "id": "{{did}}",
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

        List<Map.Entry<Block, Map.Entry<Tx, BTCR2Update>>> updates = new ArrayList<>();
        Map<Block, Map<Tx, CASAnnouncement>> casAnnouncements = new LinkedHashMap<>();
        Map<Block, Map<Tx, SMTProof>> smtProofs = new LinkedHashMap<>();
        int current_version_id = 1;
        List<BytesArray> update_hash_history = new ArrayList<>();
        Integer block_confirmations = null;

        /*
         * Decode the DID
         * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#decode-the-did
         */

        // The did MUST be parsed with the DID-BTCR2 Identifier Decoding algorithm to retrieve version, network, and genesis_bytes.

        IdentifierComponents identifierComponents = DidBtcr2IdentifierDecoding.didBtcr2IdentifierDecoding(identifier);
        BitcoinConnection bitcoinConnection = this.getBitcoinConnector().getBitcoinConnection(identifierComponents.network());

        /*
         * Process Sidecar Data
         * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#process-sidecar-data
         */

        // resolutionOptions contains a sidecar property (Sidecar Data (data structure)) which SHOULD be prepared as lookup tables:

        Map<String, Object> sidecarMap = resolutionOptions == null ? null : (Map<String, Object>) resolutionOptions.get("sidecar");
        SidecarData sidecar = sidecarMap == null ? null : SidecarData.fromMap(sidecarMap);
        if (log.isDebugEnabled()) log.debug("Sidecar data: " + sidecar);

        // Hash each BTCR2 Signed Update (data structure) in sidecar.updates with the JSON Document Hashing algorithm
        // and build a map from hash to update (update_lookup_table).

        List<BTCR2Update> sidecarUpdates = sidecar == null ? null : sidecar.getUpdates();
        Map<BytesArray, BTCR2Update> update_lookup_table = sidecarUpdates == null ? null : sidecarUpdates.stream().collect(Collectors.toMap(update -> BytesArray.bytesArray(JSONDocumentHashing.jsonDocumentHashing(update)), update -> update));

        // Hash each CAS Announcement (data structure) in sidecar.casUpdates with the JSON Document Hashing algorithm
        // and build a map from hash to announcement (cas_lookup_table).

        List<CASAnnouncement> sidecarCasAnnouncements = sidecar == null ? null : sidecar.getCasUpdates();
        Map<BytesArray, CASAnnouncement> cas_lookup_table = sidecarCasAnnouncements == null ? null : sidecarCasAnnouncements.stream().collect(Collectors.toMap(casAnnouncement -> BytesArray.bytesArray(JSONDocumentHashing.jsonDocumentHashing(casAnnouncement)), casAnnouncement -> casAnnouncement));

        // Build a map from sidecar.smtProofs keyed by proof id (smt_lookup_table).

        List<SMTProof> sidecarSmtProofs = sidecar == null ? null : sidecar.getSmtProofs();
        Map<BytesArray, SMTProof> smt_lookup_table = sidecarSmtProofs == null ? null : sidecarSmtProofs.stream().collect(Collectors.toMap(smtProof -> BytesArray.bytesArray(Base64.getUrlDecoder().decode(smtProof.getId())), smtProof -> smtProof));

        // If genesis_bytes is a SHA-256 hash, hash sidecar.genesisDocument with the JSON Document Hashing algorithm.
        // Raise an INVALID_DID error if the computed hash does not match genesis_bytes.

        Cid genesisDocumentCid = null;
        DIDDocument genesisDocument = null;

        if (GenesisBytesType.SHA256HASH == identifierComponents.genesisBytesType()) {

            genesisDocument = sidecar == null ? null : sidecar.getGenesisDocument();
            if (genesisDocument != null) {
                byte[] genesisDocumentHash = JSONDocumentHashing.jsonDocumentHashing(genesisDocument);
                if (! Arrays.equals(genesisDocumentHash, identifierComponents.genesisBytes())) {
                    throw new ResolutionException(ResolutionException.ERROR_INVALID_DID, "Computed hash " + Base64.getUrlEncoder().withoutPadding().encodeToString(genesisDocumentHash) + " does not match genesis_bytes " + Base64.getUrlEncoder().withoutPadding().encodeToString(identifierComponents.genesisBytes()));
                }
            }

            // If sidecar.genesisDocument is not provided, retrieve it from CAS using genesis_bytes

            if (genesisDocument == null && this.getIpfsConnection() != null) {
                try {
                    genesisDocumentCid = Cid.buildCidV1(Cid.Codec.Raw, Multihash.Type.sha2_256, identifierComponents.genesisBytes());
                    byte[] genesisDocumentBytes = this.getIpfsConnection().getIpfs().cat(genesisDocumentCid);
                    genesisDocument = genesisDocumentBytes == null ? null : DIDDocument.fromJson(new InputStreamReader(new ByteArrayInputStream(genesisDocumentBytes), StandardCharsets.UTF_8));
                    if (log.isDebugEnabled()) log.debug("Found genesisDocument for genesis_bytes " + Base64.getUrlEncoder().withoutPadding().encodeToString(identifierComponents.genesisBytes()) + " in CAS (IPFS) at " + genesisDocumentCid + ": " + genesisDocument);
                } catch (Exception ex) {
                    throw new ResolutionException("Cannot get genesisDocument for genesis_bytes " + Base64.getUrlEncoder().withoutPadding().encodeToString(identifierComponents.genesisBytes()) + " from CAS (IPFS) at " + genesisDocumentCid + ": " + ex.getMessage(), ex);
                }
            }
        }

        // The resolver: 1. Establishes current_document from the DID or from Sidecar Data.

        /*
         * Establish current_document
         * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#establish-current-document
         */

        // Choose how to establish current_document based on the type of genesis_bytes retrieved from the decoded did:

        DIDDocumentV1_1 current_document = switch (identifierComponents.genesisBytesType()) {

            /*
             * If genesis_bytes is a SHA-256 Hash
             * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#if-genesis_bytes-is-a-sha-256-hash
             */

            case SHA256HASH -> {

                // Process the Genesis Document provided in sidecar.genesisDocument by replacing the identifier
                // placeholder ("did:btcr2:_") with the did. Parse the result as JSON to form current_document.
                // A simple string replacement is sufficient.

                if (genesisDocument == null) {
                    throw new ResolutionException(ResolutionException.ERROR_INVALID_OPTIONS, "Missing genesis document in sidecar data and CAS");
                }
                DIDDocumentV1_1 didDocumentV1_1 = DIDDocumentV1_1.fromJson(genesisDocument.toJson().replace("did:btcr2:_", identifier.getDidString()));

                // The resulting DID Document (data structure) MUST be conformant to DID Core v1.1

                try {
                    Validation.validate(didDocumentV1_1);
                } catch (IllegalStateException ex) {
                    throw new ResolutionException(ResolutionException.ERROR_INVALID_DID_DOCUMENT, "Invalid DID document: " + ex.getMessage(), ex);
                }
                yield didDocumentV1_1;
            }

            /*
             * If genesis_bytes is a secp256k1 Public Key
             * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#if-genesis_bytes-is-a-secp256k1-public-key
             */

            case SECP256K1PUBLICKEY -> {

                // Render the Initial DID Document template with these values
                // (Bitcoin addresses MUST use the Bitcoin URI Scheme [BIP321]):

                byte[] publicKeyBytes = identifierComponents.genesisBytes();
                Network network = identifierComponents.network();

                AddressParser addressParser = AddressParser.getDefault();
                PublicKey initialPublicKey = PublicKey.parse(publicKeyBytes);

                String did = identifier.getDidString();
                String publicKeyMultikey = publicKeyMultikey(publicKeyBytes);
                URI p2pkhServiceEndpoint = URI.create(BitcoinURI.convertToBitcoinURI(addressParser.parseAddress(initialPublicKey.p2pkhAddress(new BlockHash(bitcoinConnector.getGenesisHash(network)))), null, null, null));
                URI p2wpkhServiceEndpoint = URI.create(BitcoinURI.convertToBitcoinURI(addressParser.parseAddress(initialPublicKey.p2wpkhAddress(new BlockHash(bitcoinConnector.getGenesisHash(network)))), null, null, null));
                URI p2trServiceEndpoint = URI.create(BitcoinURI.convertToBitcoinURI(addressParser.parseAddress(initialPublicKey.p2trAddress(new BlockHash(bitcoinConnector.getGenesisHash(network)))), null, null, null));

                String initialDidDocumentString = INITIAL_DID_DOCUMENT_TEMPLATE
                        .replace("{{did}}", did)
                        .replace("{{public-key-multikey}}", publicKeyMultikey)
                        .replace("{{p2pkh-bitcoin-address}}", p2pkhServiceEndpoint.toString())
                        .replace("{{p2wpkh-bitcoin-address}}", p2wpkhServiceEndpoint.toString())
                        .replace("{{p2tr-bitcoin-address}}", p2trServiceEndpoint.toString());

                // Parse the rendered template as JSON to form current_document.

                DIDDocumentV1_1 didDocumentV1_1 = DIDDocumentV1_1.fromJson(initialDidDocumentString);

                // The resulting DID Document (data structure) MUST be conformant to DID Core v1.1

                try {
                    Validation.validate(didDocumentV1_1);
                } catch (IllegalStateException ex) {
                    throw new ResolutionException(ResolutionException.ERROR_INVALID_DID_DOCUMENT, "Invalid DID document: " + ex.getMessage(), ex);
                }
                yield didDocumentV1_1;
            }
        };

        // 2. Repeats the following loop:

        Map<Block, Map<Tx, Cid>> updateCids = new LinkedHashMap<>();
        Map<Block, Map<Tx, Cid>> casAnnouncementCids = new LinkedHashMap<>();
        Map<Block, Map<Tx, Cid>> smtProofCids = new LinkedHashMap<>();

        process: do {

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
                beaconServices = beaconServices.stream().filter(BeaconType::isValid).toList();
                if (beaconServices.isEmpty()) {
                    if (log.isWarnEnabled()) log.warn("No beacon services found in current_document: {}", current_document);
                }
            }

            // Parse each beacon serviceEndpoint as a Beacon Address

            Map<String, String> beaconsAddresses = new LinkedHashMap<>();
            if (beaconServices != null) {
                for (Service beaconService : beaconServices) {
                    try {
                        String beaconAddress = AddressUtil.bitcoinUriToAddressString((URI) beaconService.getServiceEndpoint());
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

            for (Map.Entry<String, String> beaconAddressEntry : beaconsAddresses.entrySet()) {
                String beaconAddress = beaconAddressEntry.getKey();
                String beaconServiceType = beaconAddressEntry.getValue();
                for (Tx beaconTransaction : bitcoinConnection.getAddressTransactions(beaconAddress)) {
                    Block beaconBlock = bitcoinConnection.getBlockByTransaction(beaconTransaction);
                    if (beaconBlock.confirmations() < 1 || beaconBlock.blockHeight() == null || beaconBlock.blockHash() == null || beaconBlock.blockTime() == null) {
                        if (log.isDebugEnabled()) log.debug("Block {} is not complete. Skipping.", beaconBlock);
                        continue;
                    }
                    Matcher matcher = PATTERN_TX_SIGNALBYTES.matcher(beaconTransaction.txOuts().getLast().scriptPubKeyAsm());
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

            for (Tx beaconTransaction : beaconsBlocks.keySet()) {

                Block beaconBlock = beaconsBlocks.get(beaconTransaction);
                String beaconServiceType = beaconsServiceTypes.get(beaconTransaction);
                byte[] beaconSignalBytes = beaconsSignalBytes.get(beaconTransaction);

                // Derive update_hash from the transaction’s Signal Bytes based on the beacon type:

                byte[] update_hash = switch (BeaconType.fromServiceType(beaconServiceType)) {

                    case BeaconType.SINGLETON ->
                            // update_hash is the Signal Bytes.
                            beaconSignalBytes;

                    case BeaconType.CAS ->
                            // use Process CAS Beacon.
                            processCASBeacon(this.getIpfsConnection(), beaconSignalBytes, identifier, cas_lookup_table,
                                    casAnnouncement -> casAnnouncements.computeIfAbsent(beaconBlock, x -> new LinkedHashMap<>()).put(beaconTransaction, casAnnouncement),
                                    casAnnouncementCid -> casAnnouncementCids.computeIfAbsent(beaconBlock, x -> new LinkedHashMap<>()).put(beaconTransaction, casAnnouncementCid));

                    case BeaconType.SMT ->
                            // use Process SMT Beacon.
                            processSMTBeacon(this.getIpfsConnection(), beaconSignalBytes, smt_lookup_table,
                                    smtProof -> smtProofs.computeIfAbsent(beaconBlock, x -> new LinkedHashMap<>()).put(beaconTransaction, smtProof),
                                    smtProofCid -> smtProofCids.computeIfAbsent(beaconBlock, x -> new LinkedHashMap<>()).put(beaconTransaction, smtProofCid));
                };

                if (update_hash == null) {
                    if (log.isWarnEnabled()) log.warn("Transaction {} with service type {} has no update_hash. Skipping.", beaconTransaction, beaconServiceType);
                    continue;
                }

                // Build a tuple with: The transaction’s block metadata (height, time, and confirmations).
                // The BTCR2 Signed Update (data structure) retrieved from update_lookup_table[update_hash].

                BTCR2Update update = update_lookup_table == null ? null : update_lookup_table.get(BytesArray.bytesArray(update_hash));
                if (log.isDebugEnabled()) log.debug("Found update for update_hash " + Base64.getUrlEncoder().withoutPadding().encodeToString(update_hash) + " in update_lookup_table: " + update);

                // If the update is not in update_lookup_table, retrieve it from CAS.

                Cid updateCid = null;
                if (update == null && this.getIpfsConnection() != null) {
                    try {
                        updateCid = Cid.buildCidV1(Cid.Codec.Raw, Multihash.Type.sha2_256, update_hash);
                        byte[] updateBytes = this.getIpfsConnection().getIpfs().cat(updateCid);
                        update = updateBytes == null ? null : BTCR2Update.fromJson(new InputStreamReader(new ByteArrayInputStream(updateBytes), StandardCharsets.UTF_8));
                        if (log.isDebugEnabled()) log.debug("Found update for update_hash " + Base64.getUrlEncoder().withoutPadding().encodeToString(update_hash) + " in CAS (IPFS) at " + updateCid + ": " + update);
                    } catch (Exception ex) {
                        throw new ResolutionException("Cannot get update for update_hash " + Base64.getUrlEncoder().withoutPadding().encodeToString(update_hash) + " from CAS (IPFS) at " + updateCid + ": " + ex.getMessage(), ex);
                    }
                }

                if (updateCid != null) updateCids.computeIfAbsent(beaconBlock, x -> new LinkedHashMap<>()).put(beaconTransaction, updateCid);

                // Raise a MISSING_UPDATE_DATA error if the update is not available from either source.

                if (update == null) throw new ResolutionException("MISSING_UPDATE_DATA", "No update found for update_hash " + Base64.getUrlEncoder().withoutPadding().encodeToString(update_hash) + " from either update_lookup_table or CAS (IPFS).");

                Map.Entry<Block, Map.Entry<Tx, BTCR2Update>> updateTuple = Map.entry(beaconBlock, Map.entry(beaconTransaction, update));

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
                    .comparingInt((Map.Entry<Block, Map.Entry<Tx, BTCR2Update>> a) -> a.getValue().getValue().getTargetVersionId())
                    .thenComparingInt(a -> a.getKey().blockHeight()));

            // Take the first tuple.

            tuples: for (Map.Entry<Block, Map.Entry<Tx, BTCR2Update>> tuple : updates) {

                // 2. Set block_confirmations to the tuple’s block confirmations.

                block_confirmations = tuple.getKey().confirmations();

                // 3. If resolutionOptions.versionTime is provided and the tuple’s block time is more recent,
                // resolve current_document as didDocument.

                if (resolutionOptions != null && resolutionOptions.containsKey("versionTime")) {
                    long versionTime = (long) resolutionOptions.get("versionTime");
                    if (tuple.getKey().blockTime() > versionTime) {
                        if (log.isDebugEnabled()) log.debug("Block time {} is more recent than resolutionOptions.versionTime {}. Returning current_document.", tuple.getKey().blockTime(), versionTime);
                        break process;
                    }
                }

                // 4. Set update to the tuple’s BTCR2 Signed Update (data structure) and check update.targetVersionId.

                BTCR2Update update = tuple.getValue().getValue();
                current_document = checkUpdateTargetVersionId(current_document, update, identifier, current_version_id, update_hash_history);

                // 5. Increment current_version_id.

                current_version_id++;
                if (log.isDebugEnabled()) log.debug("current_version_id is now {}", current_version_id);

                // 6. If current_version_id is greater than or equal to the integer form of resolutionOptions.versionId,
                // resolve current_document as didDocument.

                if (resolutionOptions != null && resolutionOptions.containsKey("versionId")) {
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
        } while (false);

        // DID RESOLUTION METADATA

        Map<String, Object> didResolutionMetadata = new LinkedHashMap<>();
        didResolutionMetadata.put("contentType", Representations.DEFAULT_MEDIA_TYPE);
        if (bitcoinConnection != null) didResolutionMetadata.putAll(bitcoinConnection.getMetadata());
        if (this.getIpfsConnection() != null) didResolutionMetadata.putAll(this.getIpfsConnection().getMetadata());
        if (genesisDocumentCid != null) didResolutionMetadata.put("genesisDocumentCid", genesisDocumentCid.toString());
        if (! updateCids.isEmpty()) didResolutionMetadata.put("updateCids", updateCids.entrySet().stream().collect(Collectors.toMap(
                x -> x.getKey().blockHeight(), x -> x.getValue().entrySet().stream().collect(Collectors.toMap(
                        y -> y.getKey().txId(), y -> y.getValue().toString()
                ))
        )));
        if (! casAnnouncementCids.isEmpty()) didResolutionMetadata.put("casAnnouncementCids", casAnnouncementCids.entrySet().stream().collect(Collectors.toMap(
                x -> x.getKey().blockHeight(), x -> x.getValue().entrySet().stream().collect(Collectors.toMap(
                        y -> y.getKey().txId(), y -> y.getValue().toString()
                ))
        )));
        if (! smtProofCids.isEmpty()) didResolutionMetadata.put("smtProofCids", smtProofCids.entrySet().stream().collect(Collectors.toMap(
                x -> x.getKey().blockHeight(), x -> x.getValue().entrySet().stream().collect(Collectors.toMap(
                        y -> y.getKey().txId(), y -> y.getValue().toString()
                ))
        )));

        // DID DOCUMENT METADATA

        Map<String, Object> didDocumentMetadata = new LinkedHashMap<>();
        didDocumentMetadata.put("versionId", Integer.toString(current_version_id));
        didDocumentMetadata.put("nextVersionId", Integer.toString(current_version_id + 1)); // NOT IN SPEC
        didDocumentMetadata.put("confirmations", (block_confirmations == null ? null : block_confirmations.toString()));
        didDocumentMetadata.put("deactivated", current_document.getJsonObject().get("deactivated"));
        Map<String, Object> metadataIdentifierComponents = (Map<String, Object>) didDocumentMetadata.computeIfAbsent("identifierComponents", x -> new LinkedHashMap<>());
        metadataIdentifierComponents.put("version", identifierComponents.version());
        metadataIdentifierComponents.put("network", identifierComponents.network().toString());
        metadataIdentifierComponents.put("genesisBytes", Hex.encodeHexString(identifierComponents.genesisBytes()));
        metadataIdentifierComponents.put("genesisBytesTypes", identifierComponents.genesisBytesType());
        didDocumentMetadata.put("updates", updates.stream().map(x -> {
            Map<String, Object> metadataUpdate = new LinkedHashMap<>();
            metadataUpdate.put("blockHeight", x.getKey().blockHeight());
            metadataUpdate.put("blockHash", x.getKey().blockHash());
            metadataUpdate.put("blockTime", x.getKey().blockTime());
            metadataUpdate.put("txId", x.getValue().getKey().txId());
            metadataUpdate.put("targetVersionId", x.getValue().getValue().getTargetVersionId());
            metadataUpdate.put("sourceHash", x.getValue().getValue().getSourceHash());
            metadataUpdate.put("targetHash", x.getValue().getValue().getTargetHash());
            metadataUpdate.put("updateHash", Base64.getUrlEncoder().withoutPadding().encodeToString(JSONDocumentHashing.jsonDocumentHashing(x.getValue().getValue())));
            return metadataUpdate;
        }).toList());
        if (! casAnnouncements.isEmpty()) didDocumentMetadata.put("casAnnouncements", casAnnouncements.entrySet().stream().collect(Collectors.toMap(
                x -> x.getKey().blockHeight(), x -> x.getValue().entrySet().stream().collect(Collectors.toMap(
                        y -> y.getKey().txId(), y -> y.getValue()
                ))
        )));
        if (! smtProofCids.isEmpty()) didDocumentMetadata.put("smtProofs", smtProofs.entrySet().stream().collect(Collectors.toMap(
                x -> x.getKey().blockHeight(), x -> x.getValue().entrySet().stream().collect(Collectors.toMap(
                        y -> y.getKey().txId(), y -> y.getValue().toMap()
                ))
        )));

        // done

        ResolveResult resolveResult = ResolveResult.build(didResolutionMetadata, current_document, didDocumentMetadata);
        if (log.isDebugEnabled()) log.debug("resolveResult: {}", resolveResult);
        return resolveResult;
    }

    /*
     * Process CAS Beacon
     * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#process-cas-beacon
     */
    private static byte[] processCASBeacon(IPFSConnection ipfsConnection, byte[] signalBytes, DID did, Map<BytesArray, CASAnnouncement> cas_lookup_table, Consumer<CASAnnouncement> casAnnouncementConsumer, Consumer<Cid> casAnnouncementCidConsumer) throws ResolutionException {

        // Treat Signal Bytes as map_update_hash.

        byte[] map_update_hash = signalBytes;

        // Look up map_update_hash in cas_lookup_table to retrieve a CAS Announcement (data structure)

        CASAnnouncement casAnnouncement = cas_lookup_table == null ? null : cas_lookup_table.get(BytesArray.bytesArray(map_update_hash));
        if (log.isDebugEnabled()) log.debug("Found casAnnouncement for map_update_hash " + Base64.getUrlEncoder().withoutPadding().encodeToString(map_update_hash) + " in cas_lookup_table: " + casAnnouncement);

        Cid casAnnouncementCid = null;
        if (casAnnouncement == null && ipfsConnection != null) {
            try {
                casAnnouncementCid = Cid.buildCidV1(Cid.Codec.Raw, Multihash.Type.sha2_256, map_update_hash);
                byte[] casAnnouncementBytes = ipfsConnection.getIpfs().cat(casAnnouncementCid);
                casAnnouncement = casAnnouncementBytes == null ? null : CASAnnouncement.fromJson(new InputStreamReader(new ByteArrayInputStream(casAnnouncementBytes), StandardCharsets.UTF_8));
                if (log.isDebugEnabled()) log.debug("Found casAnnouncement for map_update_hash " + Base64.getUrlEncoder().withoutPadding().encodeToString(map_update_hash) + " in CAS (IPFS) at " + casAnnouncementCid + ": " + casAnnouncement);
            } catch (Exception ex) {
                throw new ResolutionException("Cannot get casAnnouncement for map_update_hash " + Base64.getUrlEncoder().withoutPadding().encodeToString(map_update_hash) + " from CAS (IPFS) at " + casAnnouncementCid + ": " + ex.getMessage(), ex);
            }
        }

        if (casAnnouncementCid != null) casAnnouncementCidConsumer.accept(casAnnouncementCid);

        if (casAnnouncement == null) throw new ResolutionException(ResolutionException.ERROR_INVALID_DID_DOCUMENT, "No CAS Announcement found for map_update_hash " + Base64.getUrlEncoder().withoutPadding().encodeToString(map_update_hash));
        casAnnouncementConsumer.accept(casAnnouncement);

        // and read update_hash from the announcement entry keyed by did.

        String update_hash_string = casAnnouncement.get(did.getDidString());
        if (update_hash_string == null) throw new ResolutionException(ResolutionException.ERROR_INVALID_DID_DOCUMENT, "No update_hash found for DID " + did + " and map_update_hash " + Base64.getUrlEncoder().withoutPadding().encodeToString(map_update_hash));

        // done

        byte[] update_hash = Base64.getUrlDecoder().decode(update_hash_string);
        if (log.isDebugEnabled()) log.debug("For did {} and map_update_hash {} found update_hash: {}", did, Base64.getUrlEncoder().withoutPadding().encodeToString(map_update_hash), Base64.getUrlEncoder().withoutPadding().encodeToString(update_hash));
        return update_hash;
    }

    /*
     * Process SMT Beacon
     * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#process-smt-beacon
     */
    private static byte[] processSMTBeacon(IPFSConnection ipfsConnection, byte[] signalBytes, Map<BytesArray, SMTProof> smt_lookup_table, Consumer<SMTProof> smtProofConsumer, Consumer<Cid> smtProofCidConsumer) throws ResolutionException {

        // Treat Signal Bytes as smt_root.

        byte[] smt_root = signalBytes;

        // Look up smt_root in smt_lookup_table to retrieve an SMT Proof (data structure).

        SMTProof smtProof = smt_lookup_table == null ? null : smt_lookup_table.get(BytesArray.bytesArray(smt_root));
        if (log.isDebugEnabled()) log.debug("Found smtProof for smt_root " + Base64.getUrlEncoder().withoutPadding().encodeToString(smt_root) + " in smt_lookup_table: " + smtProof);

        Cid smtProofCid = null;
        if (smtProof == null && ipfsConnection != null) {
            try {
                smtProofCid = Cid.buildCidV1(Cid.Codec.Raw, Multihash.Type.sha2_256, smt_root);
                byte[] smtProofBytes = ipfsConnection.getIpfs().cat(smtProofCid);
                smtProof = smtProofBytes == null ? null : SMTProof.fromJson(new InputStreamReader(new ByteArrayInputStream(smtProofBytes), StandardCharsets.UTF_8));
                if (log.isDebugEnabled()) log.debug("Found smtProof for smt_root " + Base64.getUrlEncoder().withoutPadding().encodeToString(smt_root) + " in CAS (IPFS) at " + smtProofCid + ": " + smtProof);
            } catch (Exception ex) {
                throw new ResolutionException("Cannot get smtProof for smt_root " + Base64.getUrlEncoder().withoutPadding().encodeToString(smt_root) + " from CAS (IPFS) at " + smtProofCid + ": " + ex.getMessage(), ex);
            }
        }

        if (smtProofCid != null) smtProofCidConsumer.accept(smtProofCid);

        if (smtProof == null) throw new ResolutionException(ResolutionException.ERROR_INVALID_DID_DOCUMENT, "No SMT Proof found for smt_root " + Base64.getUrlEncoder().withoutPadding().encodeToString(smt_root));
        smtProofConsumer.accept(smtProof);

        // Validate the proof with the SMT Proof Verification algorithm.

        SMTProofVerification.smtProofVerification(smtProof);

        // Use smt_proof.updateId as update_hash.

        byte[] update_hash = Base64.getUrlDecoder().decode(smtProof.getUpdateId());
        if (log.isDebugEnabled()) log.debug("For smt_root {} found update_hash: {}", Base64.getUrlEncoder().withoutPadding().encodeToString(smt_root), Base64.getUrlEncoder().withoutPadding().encodeToString(update_hash));
        return update_hash;
    }

    /*
     * Check update.targetVersionId
     * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#check-update-version
     */
    private static DIDDocumentV1_1 checkUpdateTargetVersionId(DIDDocumentV1_1 current_document, BTCR2Update update, DID did, int current_version_id, List<BytesArray> update_hash_history) throws ResolutionException {

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

        throw new IllegalStateException("Illegal update.targetVersionId " + update.getTargetVersionId() + " and current_version_id " + current_version_id);
    }

    /*
     * Confirm Duplicate Update
     * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#confirm-duplicate-update
     */
    private static void confirmDuplicateUpdate(BTCR2Update update, List<BytesArray> update_hash_history) throws ResolutionException {

        // Create unsigned_update by removing the proof property from update.

        BTCR2Update unsigned_update = BTCR2Update.fromJson(update.toJson());
        JsonLDUtils.jsonLdRemove(unsigned_update, DataIntegrityKeywords.JSONLD_TERM_PROOF);

        // Hash unsigned_update with the JSON Document Hashing algorithm

        byte[] unsignedUpdateHash = JSONDocumentHashing.jsonDocumentHashing(unsigned_update);

        // and compare it to update_hash_history[update.targetVersionId - 2].
        // Raise a LATE_PUBLISHING error if the hashes differ.

        if (! BytesArray.bytesArray(unsignedUpdateHash).equals(update_hash_history.get(update.getTargetVersionId() - 2))) {
            throw new ResolutionException("LATE_PUBLISHING", "unsigned_update hash (" + Hex.encodeHexString(unsignedUpdateHash) + ") differs from update_hash_history[" + (update.getTargetVersionId()-2) + "] (" + Hex.encodeHexString(update_hash_history.get(update.getTargetVersionId() - 2).bytes()) + ")");
        }
    }

    /*
     * Apply update
     * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#apply-update
     */
    private static DIDDocumentV1_1 applyUpdate(DIDDocumentV1_1 current_document, BTCR2Update update, DID did, List<BytesArray> update_hash_history) throws ResolutionException {

        // Hash current_document with the JSON Document Hashing algorithm.

        byte[] currentDocumentHash = JSONDocumentHashing.jsonDocumentHashing(current_document);

        // Raise an INVALID_DID_UPDATE error if the result does not match the decoded update.sourceHash.

        try {
            byte[] decodedSourceHash = Base64.getUrlDecoder().decode(update.getSourceHash());
            if (! Arrays.equals(currentDocumentHash, decodedSourceHash)) {
                throw new ResolutionException("INVALID_DID_UPDATE", "current_document hash (" + Base64.getUrlEncoder().withoutPadding().encodeToString(currentDocumentHash) + ") differs from decoded update.sourceHash (" + Base64.getUrlEncoder().withoutPadding().encodeToString(decodedSourceHash) + " decoded from " + update.getSourceHash() + ")");
            }
        } catch (IllegalArgumentException ex) {
            throw new ResolutionException("INVALID_DID_UPDATE", "Cannot decode update.sourceHash " + update.getSourceHash() + ": " + ex.getMessage(), ex);
        }

        // Check update.proof.

        checkUpdateProof(current_document, update);

        // Apply the update.patch JSON Patch [RFC6902] to current_document.

        JsonPatch jsonPatch = Json.createPatch(Json.createArrayBuilder(update.getPatch()).build());
        current_document = JSONPatchUtil.apply(current_document, jsonPatch);

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

        try {
            byte[] decodedTargetHash = Base64.getUrlDecoder().decode(update.getTargetHash());
            if (! Arrays.equals(currentDocumentHash, decodedTargetHash)) {
                throw new ResolutionException("INVALID_DID_UPDATE", "current_document hash (" + Base64.getUrlEncoder().withoutPadding().encodeToString(currentDocumentHash) + ") differs from decoded update.targetHash (" + Base64.getUrlEncoder().withoutPadding().encodeToString(decodedTargetHash) + " decoded from " + update.getTargetHash() + ")");
            }
        } catch (IllegalArgumentException ex) {
            throw new ResolutionException("INVALID_DID_UPDATE", "Cannot decode update.targetHash " + update.getTargetHash() + ": " + ex.getMessage(), ex);
        }

        // Create unsigned_update by removing the proof property from update

        BTCR2Update unsigned_update = BTCR2Update.fromJson(update.toJson());
        JsonLDUtils.jsonLdRemove(unsigned_update, DataIntegrityKeywords.JSONLD_TERM_PROOF);

        // hash it with the JSON Document Hashing algorithm

        byte[] unsignedUpdateHash = JSONDocumentHashing.jsonDocumentHashing(unsigned_update);

        // append the hash to update_hash_history.

        update_hash_history.add(BytesArray.bytesArray(unsignedUpdateHash));

        // done

        return current_document;
    }

    /*
     * Check update.proof
     * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#check-update-proof
     */
    private static void checkUpdateProof(DIDDocument current_document, BTCR2Update update) throws ResolutionException {

        DataIntegrityProof proof = DataIntegrityProof.getFromJsonLDObject(update);
        if (proof == null) {
            throw new ResolutionException("INVALID_DID_UPDATE", "Update has no proof: " + update);
        }
        if (proof.getVerificationMethod() == null) {
            throw new ResolutionException("INVALID_DID_UPDATE", "Update proof has no verification method: " + update);
        }

        // Implementations MAY derive a Root Capability (data structure) from update.proof and
        // invoke it according to Authorization Capabilities for Linked Data v0.3 [ZCAP-LD].

        RootCapability rootCapability = RootDidBtcr2UpdateCapabilities.dereferenceRootCapabilityIdentifier(proof.getCapability());
        if (! rootCapability.getInvocationTarget().equals(current_document.getId())) {
            throw new ResolutionException("INVALID_DID_UPDATE", "Root capability 'invocationTarget' " + rootCapability.getInvocationTarget() + " does not match contemporary DID document 'id': " + current_document.getId());
        }
        if (! rootCapability.getController().equals(current_document.getId())) {
            throw new ResolutionException("INVALID_DID_UPDATE", "Root capability 'controller' " + rootCapability.getInvocationTarget() + " does not match contemporary DID document 'id': " + current_document.getId());
        }

        // The resolver must locate publicKeyMultibase in current_document.verificationMethod
        // whose id matches update.proof.verificationMethod. Otherwise raise INVALID_DID_UPDATE.

        JsonLDObject verificationMethodJsonLDObject = JsonLDDereferencer.findByIdInJsonLdObject(current_document, proof.getVerificationMethod(), current_document.getId());
        VerificationMethod verificationMethod = verificationMethodJsonLDObject == null ? null : VerificationMethod.fromJsonObject(verificationMethodJsonLDObject.getJsonObject());
        if (verificationMethod == null) {
            throw new ResolutionException("INVALID_DID_UPDATE", "Cannot find verification method whose id matches update.proof.verificationMethod: " + proof.getVerificationMethod());
        }
        byte[] publicKeyBytes = MulticodecDecoder.getInstance().decode(Multibase.decode(verificationMethod.getPublicKeyMultibase()));
        if (log.isDebugEnabled()) log.debug("Public key bytes for verification method {}: {}", proof.getVerificationMethod(), Hex.encodeHexString(publicKeyBytes));

        // Raise the same error if current_document.capabilityInvocation does not contain update.proof.verificationMethod.

        List<VerificationMethod> capabilityInvocationVerificationMethod = current_document.getCapabilityInvocationVerificationMethodsDereferenced();
        if (! capabilityInvocationVerificationMethod.contains(verificationMethod)) {
            throw new ResolutionException("INVALID_DID_UPDATE", "current_document.capabilityInvocation does not contain update.proof.verificationMethod " + proof.getVerificationMethod());
        }

        // Use a BIP340 Cryptosuite [BIP340-Cryptosuite] instance with publicKeyMultibase
        // and the "bip340-jcs-2025" cryptosuite to verify update. Raise INVALID_DID_UPDATE
        // if verification fails.

        DataIntegrityProofLdVerifier dataIntegrityProofLdVerifier = (DataIntegrityProofLdVerifier) LdVerifierRegistry.getLdVerifierByDataIntegritySuiteTerm(proof.getType());
        dataIntegrityProofLdVerifier.setVerifier(new secp256k1_ES256KS_PublicKeyVerifier(ECKey.fromPublicOnly(publicKeyBytes)));
        try {
            dataIntegrityProofLdVerifier.verify(update, proof);
        } catch (IOException | GeneralSecurityException | JsonLDException ex) {
            throw new ResolutionException("INVALID_DID_UPDATE", "Cannot verify update " + update + ": " + ex.getMessage(), ex);
        }
    }

    /*
     * Helper methods
     */

    public static final byte[] MULTICODEC_SECP256K1_PUB = new byte[] { (byte)0xe7, (byte)0x01 };    //  0xe7

    private static String publicKeyMultikey(byte[] publicKeyBytes) {
        byte[] multicodec = new byte[MULTICODEC_SECP256K1_PUB.length + publicKeyBytes.length];
        System.arraycopy(MULTICODEC_SECP256K1_PUB, 0, multicodec, 0, MULTICODEC_SECP256K1_PUB.length);
        System.arraycopy(publicKeyBytes, 0, multicodec, 2, publicKeyBytes.length);
        return Multibase.encode(Multibase.Base.Base58BTC, multicodec);
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
