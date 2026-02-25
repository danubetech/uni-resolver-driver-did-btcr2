package uniresolver.driver.did.btcr2.regtest.k1;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import org.erdtman.jcs.JsonCanonicalizer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uniresolver.driver.did.btcr2.crud.read.Read;
import uniresolver.driver.did.btcr2.syntax.DidBtcr2IdentifierDecoding;
import uniresolver.driver.did.btcr2.syntax.records.IdentifierComponents;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractResolve1Test {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private final String relativePath;

	protected AbstractResolve1Test(String relativePath) {
		this.relativePath = relativePath;
	}

	@Test
	public void testResolveTargetDIDDocument() throws Exception {

		TestUtil.Input input = TestUtil.readResourceInput(this.relativePath + "1/" + "input.json");
		TestUtil.Output output = TestUtil.readResourceOutput(this.relativePath + "1/" + "output.json");

		Read read = new Read(TestUtil.testBitcoinConnections(), TestUtil.testIpfsConnection());

		DID identifier = DID.fromString(input.did());
        Map<String, Object> resolutionOptions = input.sidecar();
		Map<String, Object> didDocumentMetadata = new HashMap<>();

		IdentifierComponents identifierComponents = DidBtcr2IdentifierDecoding.didBtcr2IdentifierDecoding(identifier);

		DIDDocument initialDIDDocument = read.getResolveInitialDocument().resolveInitialDIDDocument(identifier, identifierComponents, resolutionOptions, didDocumentMetadata);

		DIDDocument targetDIDDocument = read.getResolveTargetDocument().resolveTargetDocument(initialDIDDocument, resolutionOptions, identifierComponents.network(), didDocumentMetadata);
		String targetDIDDocumentCanonicalized = new JsonCanonicalizer(targetDIDDocument.toJson()).getEncodedString();
		Map<String, Object> targetDIDDocumentMap = (Map<String, Object>) objectMapper.readValue(targetDIDDocumentCanonicalized, Map.class);

		DIDDocument expectedTargetDIDDocument = output.didDocument();
		String expectedTargetDIDDocumentCanonicalized = new JsonCanonicalizer(expectedTargetDIDDocument.toJson()).getEncodedString();
		Map<String, Object> expectedTargetDIDDocumentMap = (Map<String, Object>) objectMapper.readValue(expectedTargetDIDDocumentCanonicalized, Map.class);

		assertEquals(expectedTargetDIDDocumentCanonicalized, targetDIDDocumentCanonicalized);
		assertEquals(expectedTargetDIDDocumentMap, targetDIDDocumentMap);
	}
}
