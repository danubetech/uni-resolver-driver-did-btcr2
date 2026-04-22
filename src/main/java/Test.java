import com.danubetech.btc.syntax.IdentifierComponents;
import foundation.identity.did.DID;
import io.ipfs.multihash.Multihash;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import uniresolver.driver.did.btcr2.algorithms.JSONDocumentHashing;
import uniresolver.driver.did.btcr2.syntax.DidBtcr2IdentifierDecoding;
import uniresolver.driver.did.btcr2.util.IPFSCIDUtil;

import java.nio.charset.StandardCharsets;

public class Test {

    private static final String GEN =
            """
                    {"@context":["https://www.w3.org/ns/did/v1","https://btcr2.dev/context/v1"],"assertionMethod":["#initialKey"],"authentication":["#initialKey"],"capabilityDelegation":["#initialKey"],"capabilityInvocation":["#initialKey"],"id":"did:btcr2:_","service":[{"id":"#myservice","serviceEndpoint":"test","type":"DIDCommMessaging"},{"id":"#initialP2PKH","serviceEndpoint":"bitcoin:n4qSxovZsCmCetjZ4Qu3kzqMT3s6a81ef7","type":"SingletonBeacon"},{"id":"#initialP2WPKH","serviceEndpoint":"bitcoin:tb1qll9yld92jq4k926hdh9ud7xdvz54sttpc00c9j","type":"SingletonBeacon"},{"id":"#initialP2TR","serviceEndpoint":"bitcoin:tb1pp6gr944zqkq92pfw94lmelnhg2vmsnuvfvsypnmvtm8gxz8u02ss2pylfj","type":"SingletonBeacon"}],"verificationMethod":[{"id":"#initialKey","publicKeyMultibase":"zQ3shedyCvmzZpLGAbaM3T4p9g6Qt1WTZnxWKjfs8uS1zWJH1","type":"Multikey"}]}
                    """;


    private static final String GEN2 =
            """
                    {"@context":["https://www.w3.org/ns/did/v1","https://btcr2.dev/context/v1"],"assertionMethod":["#initialKey"],"authentication":["#initialKey"],"capabilityDelegation":["#initialKey"],"capabilityInvocation":["#initialKey"],"id":"did:btcr2:_","service":[{"id":"#myservice","serviceEndpoint":"test","type":"DIDCommMessaging"},{"id":"#initialP2PKH","serviceEndpoint":"bitcoin:n4qSxovZsCmCetjZ4Qu3kzqMT3s6a81ef7","type":"SingletonBeacon"},{"id":"#initialP2WPKH","serviceEndpoint":"bitcoin:tb1qll9yld92jq4k926hdh9ud7xdvz54sttpc00c9j","type":"SingletonBeacon"},{"id":"#initialP2TR","serviceEndpoint":"bitcoin:tb1pp6gr944zqkq92pfw94lmelnhg2vmsnuvfvsypnmvtm8gxz8u02ss2pylfj","type":"SingletonBeacon"}],"verificationMethod":[{"id":"#initialKey","publicKeyMultibase":"zQ3shedyCvmzZpLGAbaM3T4p9g6Qt1WTZnxWKjfs8uS1zWJH1","type":"Multikey"}]}
                    """;

    public static void main(String[] args) throws Exception {
        IdentifierComponents identifierComponents = DidBtcr2IdentifierDecoding.didBtcr2IdentifierDecoding(DID.fromString("did:btcr2:x1q4ehfnsysdhf0u2zq57y4h7jykxkkwrtnxypj9ymda4nfhqn9pxvqwu0x6v"));
        System.out.println(Hex.encodeHexString(identifierComponents.genesisBytes()));
        System.out.println(Hex.encodeHexString(JSONDocumentHashing.jsonDocumentHashing(GEN)));
        System.out.println(Hex.encodeHexString(JSONDocumentHashing.jsonDocumentHashing(GEN2)));
        System.out.println(Base64.encodeBase64String(identifierComponents.genesisBytes()));
        System.out.println(Hex.encodeHexString(new Multihash(Multihash.Type.sha2_256, identifierComponents.genesisBytes()).getHash()));
        System.out.println(Hex.encodeHexString(Multihash.fromBase58("QmdoofWDm9vZpSQr2ogCJp9FKxmvDdEYaXPGhH4Pmw9vFM").getHash()));
        System.out.println("----------->  " + IPFSCIDUtil.createCid(0, JSONDocumentHashing.jsonDocumentCanonicalizing(GEN).getBytes(StandardCharsets.UTF_8)));
    }
}
