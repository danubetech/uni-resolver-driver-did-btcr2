package uniresolver.driver.did.btc1.crud.read;

import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btc1.connections.bitcoin.BitcoinConnection;
import uniresolver.driver.did.btc1.connections.ipfs.IPFSConnection;
import uniresolver.driver.did.btc1.crud.read.records.IdentifierComponents;

import java.util.Map;

public class Read {

    private static final Logger log = LoggerFactory.getLogger(Read.class);

    private ResolveInitialDocument resolveInitialDocument;
    private ResolveTargetDocument resolveTargetDocument;

    public Read(BitcoinConnection bitcoinConnection, IPFSConnection ipfsConnection) {
        this.resolveInitialDocument = new ResolveInitialDocument(bitcoinConnection, ipfsConnection);
        this.resolveTargetDocument = new ResolveTargetDocument(bitcoinConnection, ipfsConnection);
    }

    /*
     * 4.2 Read
     */

    public DIDDocument read(DID identifier, Map<String, Object> resolutionOptions, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) throws ResolutionException {

        IdentifierComponents identifierComponents = ParseDidBtc1Identifier.parseDidBtc1Identifier(identifier, /* TODO: extra, not in spec */ didDocumentMetadata);
        if (log.isDebugEnabled()) log.debug("Parsed identifier: " + identifierComponents);

        DIDDocument initialDocument = this.resolveInitialDocument.resolveInitialDIDDocument(identifier, identifierComponents, resolutionOptions, /* TODO: extra, not in spec */ didDocumentMetadata);

        DIDDocument targetDocument = this.resolveTargetDocument.resolveTargetDocument(initialDocument, resolutionOptions, /* TODO: extra, not in spec */ identifierComponents.network(), didDocumentMetadata);

        if (log.isDebugEnabled()) log.debug("read: " + targetDocument);
        return targetDocument;
    }
}
