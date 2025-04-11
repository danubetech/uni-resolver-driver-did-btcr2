package uniresolver.driver.did.btc1;

import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import uniresolver.driver.did.btc1.crud.read.ResolveInitialDocument;
import uniresolver.driver.did.btc1.syntax.DidBtc1IdentifierDecoding;
import uniresolver.driver.did.btc1.syntax.records.IdentifierComponents;

import java.util.Collections;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws Exception {
        String did = "did:btc1:k1qgp8lpuyxprky2kh24hdxlycrcyk56lkqmnuelpw70d3ay2gej6vhwgfqurtz";
        IdentifierComponents identifierComponents = DidBtc1IdentifierDecoding.didBtc1IdentifierDecoding(DID.fromString(did));
        DIDDocument didDocument = new ResolveInitialDocument(null, null).resolveInitialDIDDocument(DID.fromString(did), identifierComponents, Collections.emptyMap(), new HashMap<>());
        System.out.println(didDocument);
    }
}
