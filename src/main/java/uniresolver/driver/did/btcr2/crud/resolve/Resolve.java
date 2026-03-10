package uniresolver.driver.did.btcr2.crud.resolve;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import fr.acinq.bitcoin.BlockHash;
import io.ipfs.multibase.binary.Base64;
import io.leonard.Base58;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.base.AddressParser;
import org.bitcoinj.uri.BitcoinURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btcr2.Network;
import uniresolver.driver.did.btcr2.algorithms.JSONDocumentHashing;
import uniresolver.driver.did.btcr2.connections.bitcoin.BitcoinConnector;
import uniresolver.driver.did.btcr2.connections.ipfs.IPFSConnection;
import uniresolver.driver.did.btcr2.data.json.CASAnnouncement;
import uniresolver.driver.did.btcr2.data.json.SMTProof;
import uniresolver.driver.did.btcr2.data.json.SidecarData;
import uniresolver.driver.did.btcr2.data.jsonld.BTCR2Update;
import uniresolver.driver.did.btcr2.data.records.GenesisBytesType;
import uniresolver.driver.did.btcr2.data.records.IdentifierComponents;
import uniresolver.driver.did.btcr2.syntax.DidBtcr2IdentifierDecoding;
import uniresolver.result.ResolveResult;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private static final Logger log = LoggerFactory.getLogger(Resolve.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

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

/*        DIDDocument current_document;
        int current_version_id = 1;
        List<byte[]> update_hash_history = new ArrayList<>();
        List<Object> block_confirmations = new ArrayList<>();*/

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

        Map<String, Object> sidecarMap = (Map<String, Object>) resolutionOptions.get("sidecar");
        SidecarData sidecar = sidecarMap == null ? null : objectMapper.convertValue(sidecarMap, SidecarData.class);

        // Hash each BTCR2 Signed Update (data structure) in sidecar.updates with the JSON Document Hashing algorithm
        // and build a map from hash to update (update_lookup_table).

        List<BTCR2Update> updates = sidecar == null ? null : sidecar.getUpdates();
        Map<byte[], BTCR2Update> update_lookup_table = updates == null ? null : updates.stream().collect(Collectors.toMap(JSONDocumentHashing::jsonDocumentHashing, btcr2Update -> btcr2Update));

        // Hash each CAS Announcement (data structure) in sidecar.casUpdates with the JSON Document Hashing algorithm
        // and build a map from hash to announcement (cas_lookup_table).

        List<CASAnnouncement> casUpdates = sidecar == null ? null : sidecar.getCasUpdates();
        Map<byte[], CASAnnouncement> cas_lookup_table = casUpdates == null ? null : casUpdates.stream().collect(Collectors.toMap(JSONDocumentHashing::jsonDocumentHashing, casAnnouncement -> casAnnouncement));

        // Build a map from sidecar.smtProofs keyed by proof id (smt_lookup_table).

        List<SMTProof> smtProofs = sidecar == null ? null : sidecar.getSmtProofs();
        Map<String, SMTProof> smt_lookup_table = smtProofs == null ? null : smtProofs.stream().collect(Collectors.toMap(SMTProof::getId, smtProof -> smtProof));

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

        /*
         * Establish current_document
         * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#establish-current-document
         */

        // Resolution begins by creating an Initial Did Document called current_document (Current DID Document).

        DIDDocument current_document = null;

        /*
         * If genesis_bytes is a SHA-256 Hash
         * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#if-genesis_bytes-is-a-sha-256-hash
         */

        if (GenesisBytesType.SHA256HASH == identifierComponents.genesisBytesType()) {

            // Process the Genesis Document provided in sidecar.genesisDocument by replacing the identifier
            // placeholder ("did:btcr2:_") with the did.

            DIDDocument genesisDocument = sidecar.getGenesisDocument();
            if (genesisDocument == null) throw new ResolutionException(ResolutionException.ERROR_INVALID_OPTIONS, "Missing genesis document in sidecar data");
            current_document = DIDDocument.fromJson(sidecar.getGenesisDocument().toJson().replace("did:btcr2:_", identifier.getDidString()));
        }

        /*
         * If genesis_bytes is a secp256k1 Public Key
         * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#if-genesis_bytes-is-a-secp256k1-public-key
         */

        if (GenesisBytesType.SECP256K1PUBLICKEY == identifierComponents.genesisBytesType()) {

            // Render the Initial DID Document template with these values
            // (Bitcoin addresses MUST use the Bitcoin URI Scheme [BIP321]):

            byte[] publicKeyBytes = identifierComponents.genesisBytes();
            AddressParser addressParser = AddressParser.getDefault();
            Network network = identifierComponents.network();
            fr.acinq.bitcoin.PublicKey initialPublicKey = fr.acinq.bitcoin.PublicKey.parse(publicKeyBytes);

            String did = identifier.getDidString();
            String public_key_multikey = Base58.encode(publicKeyBytes);
            String p2pkh_bitcoin_address = BitcoinURI.convertToBitcoinURI(network.toBitcoinjNetwork(), AddressParser.getDefault().parseAddress(initialPublicKey.p2pkhAddress(new BlockHash(this.getBitcoinConnector().getGensisHash(network)))).toString(), null, null, null);
            String p2wpkh_bitcoin_address = BitcoinURI.convertToBitcoinURI(network.toBitcoinjNetwork(), AddressParser.getDefault().parseAddress(initialPublicKey.p2wpkhAddress(new BlockHash(this.getBitcoinConnector().getGensisHash(network)))).toString(), null, null, null);
            String p2tr_bitcoin_address = BitcoinURI.convertToBitcoinURI(network.toBitcoinjNetwork(), AddressParser.getDefault().parseAddress(initialPublicKey.p2trAddress(new BlockHash(this.getBitcoinConnector().getGensisHash(network)))).toString(), null, null, null);

            String initialDidDocumentString = INITIAL_DID_DOCUMENT_TEMPLATE
                    .replace("{{did}}", did)
                    .replace("{{public-key-multikey}}", public_key_multikey)
                    .replace("{{p2pkh-bitcoin-address}}", p2pkh_bitcoin_address)
                    .replace("{{p2wpkh-bitcoin-address}}", p2wpkh_bitcoin_address)
                    .replace("{{p2tr-bitcoin-address}}", p2tr_bitcoin_address);

            // Parse the rendered template as JSON to form current_document.

            current_document = DIDDocument.fromJson(initialDidDocumentString);
        }

        /*
         * Process Beacon Signals
         * See https://dcdpr.github.io/did-btcr2/operations/resolve.html#process-beacon-signals
         */

        // TODO

        // didResolutionMetadata

        Map<String, Object> didResolutionMetadata = new LinkedHashMap<>();

        // didDocumentMetadata

        Map<String, Object> didDocumentMetadata = new LinkedHashMap<>();
        didDocumentMetadata.put("identifierComponents", Map.of(
                "version", identifierComponents.version(),
                "network", identifierComponents.network().toString(),
                "genesisBytes", Hex.encodeHexString(identifierComponents.genesisBytes()),
                "genesisBytesTypes", identifierComponents.genesisBytesType()));

        // done

        ResolveResult resolveResult = ResolveResult.build(didResolutionMetadata, current_document, didDocumentMetadata);
        if (log.isDebugEnabled()) log.debug("resolve: " + current_document);
        return resolveResult;
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
