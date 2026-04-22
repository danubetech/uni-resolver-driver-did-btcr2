import com.danubetech.btc.syntax.IdentifierComponents;
import foundation.identity.did.DID;
import io.ipfs.cid.Cid;
import io.ipfs.multihash.Multihash;
import org.apache.commons.codec.binary.Hex;
import uniresolver.driver.did.btcr2.algorithms.JSONDocumentHashing;
import uniresolver.driver.did.btcr2.syntax.DidBtcr2IdentifierDecoding;
import uniresolver.driver.did.btcr2.util.SHA256Util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Test {

    private static final String D = "did:btcr2:x1qhnut0ws396g09upqnjm3pgd9svs3m55rxddswgnz2wrf3jqqya8zqqjs0a";

    private static final String GEN =
            """
                    {"verificationMethod":[{"type":"Multikey","id":"#initialKey","publicKeyMultibase":"zQ3shtPM8NeUXGGceeSgKHdshg4J17H9HEJVFnVT4dXNFyuRH"}],"service":[{"id":"#didcomm","type":"DIDCommMessaging","serviceEndpoint":"https://test.example.com/didcomm"},{"type":"SingletonBeacon","id":"#initialP2PKH","serviceEndpoint":"bitcoin:n2HzBbHRpGhd3SFrgMUXbzhe46sYJEwXFi"},{"type":"SingletonBeacon","id":"#initialP2WPKH","serviceEndpoint":"bitcoin:tb1qu0nv77kqjnvdeldqvk9v25kzgxmt05mc9aym7t"},{"type":"SingletonBeacon","id":"#initialP2TR","serviceEndpoint":"bitcoin:tb1p7fkjtm3djjjlcgeh2f2lnn7dmuvljvu2akv0py9xcg7sc6d9f0estydl0h"}],"assertionMethod":["#initialKey"],"capabilityDelegation":["#initialKey"],"capabilityInvocation":["#initialKey"],"@context":["https://www.w3.org/ns/did/v1","https://btcr2.dev/context/v1"],"authentication":["#initialKey"],"id":"did:btcr2:_"}
                    """;


    public static void main(String[] args) throws Exception {
        IdentifierComponents identifierComponents = DidBtcr2IdentifierDecoding.didBtcr2IdentifierDecoding(DID.fromString(D));
        System.out.println(Hex.encodeHexString(identifierComponents.genesisBytes()));
        System.out.println(Hex.encodeHexString(JSONDocumentHashing.jsonDocumentHashing(GEN)));
        System.out.println(":::::::::::::  " + Base64.getUrlEncoder().withoutPadding().encodeToString(identifierComponents.genesisBytes()));
        System.out.println("----------->  " + Cid.buildCidV1(Cid.Codec.Raw, Multihash.Type.sha2_256, JSONDocumentHashing.jsonDocumentHashing(GEN)));
        System.out.println("----------->  " + Cid.buildCidV1(Cid.Codec.Raw, Multihash.Type.sha2_256, identifierComponents.genesisBytes()));
    }
}
