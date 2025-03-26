package uniresolver.driver.did.btc1.crud.read;

import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import io.ipfs.api.IPFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btc1.crud.read.records.IdentifierComponents;

import java.util.Map;

public class Read {

    private static final Logger log = LoggerFactory.getLogger(Read.class);

    private IPFS ipfs;
    private ResolveInitialDocument resolveInitialDocument;
    private ResolveTargetDocument resolveTargetDocument;

    public Read(IPFS ipfs) {
        this.ipfs = ipfs;
        this.resolveInitialDocument = new ResolveInitialDocument(this.ipfs);
        this.resolveTargetDocument = new ResolveTargetDocument(this.ipfs);
    }

    /*
     * 4.2 Read
     */

    public DIDDocument read(DID identifier, Map<String, Object> resolutionOptions) throws ResolutionException {

        IdentifierComponents identifierComponents = ParseDidBtc1Identifier.parseDidBtc1Identifier(identifier);
        if (log.isDebugEnabled()) log.debug("Parsed identifier: " + identifierComponents);

        DIDDocument initialDocument = this.resolveInitialDocument.resolveInitialDIDDocument(identifier, identifierComponents, resolutionOptions);

        // TODO: disable, while work-in-progress
        DIDDocument targetDocument = this.resolveTargetDocument.resolveTargetDocument(initialDocument, resolutionOptions, /* TODO: extra, not in spec */ identifierComponents.network());

        if (log.isDebugEnabled()) log.debug("read: " + targetDocument);
        return targetDocument;
    }

    /*
     * Getters and setters
     */

    public IPFS getIpfs() {
        return this.ipfs;
    }

    public void setIpfs(IPFS ipfs) {
        this.ipfs = ipfs;
    }
}
