package uniresolver.driver.did.btc1.crud.read;

import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btc1.connections.bitcoin.BitcoinConnector;
import uniresolver.driver.did.btc1.connections.ipfs.IPFSConnection;
import uniresolver.driver.did.btc1.syntax.DidBtc1IdentifierDecoding;
import uniresolver.driver.did.btc1.syntax.records.IdentifierComponents;

import java.util.Map;

public class Read {

    private static final Logger log = LoggerFactory.getLogger(Read.class);

    private ResolveInitialDocument resolveInitialDocument;
    private ResolveTargetDocument resolveTargetDocument;

    public Read(BitcoinConnector bitcoinConnector, IPFSConnection ipfsConnection) {
        this.resolveInitialDocument = new ResolveInitialDocument(this, bitcoinConnector, ipfsConnection);
        this.resolveTargetDocument = new ResolveTargetDocument(this, bitcoinConnector, ipfsConnection);
    }

    /*
     * 4.2 Read
     */

    // See https://dcdpr.github.io/did-btc1/#read
    public DIDDocument read(DID identifier, Map<String, Object> resolutionOptions, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) throws ResolutionException {

        // Pass identifier to the did:btc1 Identifier Decoding algorithm, retrieving idType, version, network, and genesisBytes.
        // Set identifierComponents to a map of idType, version, network, and genesisBytes.

        IdentifierComponents identifierComponents = DidBtc1IdentifierDecoding.didBtc1IdentifierDecoding(identifier);

        // DID DOCUMENT METADATA

        didDocumentMetadata.put("identifierComponents", Map.of(
                "idType", identifierComponents.idType(),
                "version", identifierComponents.version(),
                "network", identifierComponents.network().toString(),
                "genesisBytes", Hex.encodeHexString(identifierComponents.genesisBytes())));

        // Set initialDocument to the result of running the algorithm in Resolve Initial Document passing in the identifier,
        // identifierComponents and resolutionOptions.

        DIDDocument initialDocument = this.resolveInitialDocument.resolveInitialDIDDocument(identifier, identifierComponents, resolutionOptions, /* TODO: extra, not in spec */ didDocumentMetadata);

        // Set targetDocument to the result of running the algorithm in Resolve Target Document passing in initialDocument
        // and resolutionOptions.

        DIDDocument targetDocument = this.resolveTargetDocument.resolveTargetDocument(initialDocument, resolutionOptions, /* TODO: extra, not in spec */ identifierComponents.network(), didDocumentMetadata);

        // Return targetDocument.

        if (log.isDebugEnabled()) log.debug("Read: " + targetDocument);
        return targetDocument;
    }

    /*
     * Getters and settes
     */

    public ResolveInitialDocument getResolveInitialDocument() {
        return resolveInitialDocument;
    }

    public void setResolveInitialDocument(ResolveInitialDocument resolveInitialDocument) {
        this.resolveInitialDocument = resolveInitialDocument;
    }

    public ResolveTargetDocument getResolveTargetDocument() {
        return resolveTargetDocument;
    }

    public void setResolveTargetDocument(ResolveTargetDocument resolveTargetDocument) {
        this.resolveTargetDocument = resolveTargetDocument;
    }
}
