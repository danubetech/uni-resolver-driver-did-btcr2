package uniresolver.driver.did.btcr2.regtest.k1qgp7vmk76hx8nnjkzym5apyps76ycvf9uggcdyakc0942kccgq5vp0cnnv5l5;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import org.erdtman.jcs.JsonCanonicalizer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uniresolver.driver.did.btcr2.crud.read.Read;
import uniresolver.driver.did.btcr2.syntax.DidBtcr2IdentifierDecoding;
import uniresolver.driver.did.btcr2.syntax.records.IdentifierComponents;
import uniresolver.driver.did.btcr2.util.JSONUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResolveTargetDocumentTest {

	private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TEST_DID = "did:btcr2:k1qgp7vmk76hx8nnjkzym5apyps76ycvf9uggcdyakc0942kccgq5vp0cnnv5l5";

	@Test
    @Disabled("Only works with configured Esplora/Electrs REST API")
	public void testResolveTargetDIDDocument() throws Exception {

		Map<String, Object> didDocumentMetadata = new HashMap<>();

		Read read = new Read(TestUtil.testBitcoinConnections(), TestUtil.testIpfsConnection());

        DID identifier = DID.fromString(TEST_DID);
        Map<String, Object> resolutionOptions = TestUtil.readResourceJson("resolution-options.json");

		IdentifierComponents identifierComponents = DidBtcr2IdentifierDecoding.didBtcr2IdentifierDecoding(identifier);

		DIDDocument initialDIDDocument = read.getResolveInitialDocument().resolveInitialDIDDocument(identifier, identifierComponents, resolutionOptions, didDocumentMetadata);

		DIDDocument targetDIDDocument = read.getResolveTargetDocument().resolveTargetDocument(initialDIDDocument, resolutionOptions, identifierComponents.network(), didDocumentMetadata);
		String targetDIDDocumentCanonicalized = new JsonCanonicalizer(targetDIDDocument.toJson()).getEncodedString();
		Map<String, Object> targetDIDDocumentMap = (Map<String, Object>) objectMapper.readValue(targetDIDDocumentCanonicalized, Map.class);

		DIDDocument expectedTargetDIDDocument = DIDDocument.fromMap(TestUtil.readResourceJson("target-did-document.json"));
		String expectedTargetDIDDocumentCanonicalized = new JsonCanonicalizer(expectedTargetDIDDocument.toJson()).getEncodedString();
		Map<String, Object> expectedTargetDIDDocumentMap = (Map<String, Object>) objectMapper.readValue(expectedTargetDIDDocumentCanonicalized, Map.class);

		assertEquals(expectedTargetDIDDocumentCanonicalized, targetDIDDocumentCanonicalized);
		assertEquals(expectedTargetDIDDocumentMap, targetDIDDocumentMap);

		System.out.println(JSONUtil.mapToJson(didDocumentMetadata));
	}
}
