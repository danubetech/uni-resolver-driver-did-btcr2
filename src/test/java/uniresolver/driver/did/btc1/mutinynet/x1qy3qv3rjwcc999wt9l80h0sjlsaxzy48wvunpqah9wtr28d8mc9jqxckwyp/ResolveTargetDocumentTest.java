package uniresolver.driver.did.btc1.mutinynet.x1qy3qv3rjwcc999wt9l80h0sjlsaxzy48wvunpqah9wtr28d8mc9jqxckwyp;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import org.erdtman.jcs.JsonCanonicalizer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uniresolver.driver.did.btc1.crud.read.Read;
import uniresolver.driver.did.btc1.syntax.DidBtc1IdentifierDecoding;
import uniresolver.driver.did.btc1.syntax.records.IdentifierComponents;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResolveTargetDocumentTest {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@Disabled("Only works with configured IPFS connection")
	public void testResolveTargetDIDDocument() throws Exception {

		Map<String, Object> didDocumentMetadata = new HashMap<>();

		Read read = new Read(TestUtil.testBitcoinConnections(), TestUtil.testIpfsConnection());
		read.getResolveInitialDocument().setHints(Map.of("initialP2TR", "tb1p5ss9d8e4rtehk32ldjtdpm38vj29yx3gwuad94q6zpx3udk8nh0q58zeeq"));

		DID identifier = DID.fromString(TestUtil.readResourceString("did.txt"));
		Map<String, Object> resolutionOptions = TestUtil.readResourceJson("resolutionOptions.json");

		IdentifierComponents identifierComponents = DidBtc1IdentifierDecoding.didBtc1IdentifierDecoding(identifier);

		DIDDocument initialDIDDocument = read.getResolveInitialDocument().resolveInitialDIDDocument(identifier, identifierComponents, resolutionOptions, didDocumentMetadata);

		DIDDocument targetDIDDocument = read.getResolveTargetDocument().resolveTargetDocument(initialDIDDocument, resolutionOptions, identifierComponents.network(), didDocumentMetadata);
		String targetDIDDocumentCanonicalized = new JsonCanonicalizer(targetDIDDocument.toJson()).getEncodedString();
		Map<String, Object> targetDIDDocumentMap = (Map<String, Object>) objectMapper.readValue(targetDIDDocumentCanonicalized, Map.class);

		DIDDocument expectedTargetDIDDocument = DIDDocument.fromMap(TestUtil.readResourceJson("targetDocument.json"));
		String expectedTargetDIDDocumentCanonicalized = new JsonCanonicalizer(expectedTargetDIDDocument.toJson()).getEncodedString();
		Map<String, Object> expectedTargetDIDDocumentMap = (Map<String, Object>) objectMapper.readValue(expectedTargetDIDDocumentCanonicalized, Map.class);

		assertEquals(expectedTargetDIDDocumentCanonicalized, targetDIDDocumentCanonicalized);
		assertEquals(expectedTargetDIDDocumentMap, targetDIDDocumentMap);
	}
}
