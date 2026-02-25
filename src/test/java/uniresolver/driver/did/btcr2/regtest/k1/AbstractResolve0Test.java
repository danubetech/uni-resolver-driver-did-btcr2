package uniresolver.driver.did.btcr2.regtest.k1;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import org.erdtman.jcs.JsonCanonicalizer;
import org.junit.jupiter.api.Test;
import uniresolver.driver.did.btcr2.crud.read.Read;
import uniresolver.driver.did.btcr2.syntax.DidBtcr2IdentifierDecoding;
import uniresolver.driver.did.btcr2.syntax.records.IdentifierComponents;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractResolve0Test {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private final String relativePath;

	protected AbstractResolve0Test(String relativePath) {
		this.relativePath = relativePath;
	}

	@Test
	public void testResolveInitialDIDDocument() throws Exception {

		TestUtil.Input input = TestUtil.readResourceInput(this.relativePath + "0/" + "input.json");
		TestUtil.Output output = TestUtil.readResourceOutput(this.relativePath + "0/" + "output.json");

		Read read = new Read(TestUtil.testBitcoinConnections(), TestUtil.testIpfsConnection());

		DID identifier = DID.fromString(input.did());
		Map<String, Object> resolutionOptions = input.sidecar();
		Map<String, Object> didDocumentMetadata = new HashMap<>();

		IdentifierComponents identifierComponents = DidBtcr2IdentifierDecoding.didBtcr2IdentifierDecoding(identifier);

		DIDDocument initialDIDDocument = read.getResolveInitialDocument().resolveInitialDIDDocument(identifier, identifierComponents, resolutionOptions, didDocumentMetadata);
		String initialDIDDocumentCanonicalized = new JsonCanonicalizer(initialDIDDocument.toJson()).getEncodedString();
		Map<String, Object> initialDIDDocumentMap = (Map<String, Object>) objectMapper.readValue(initialDIDDocumentCanonicalized, Map.class);

		DIDDocument expectedInitialDIDDocument = output.didDocument();
		String expectedInitialDIDDocumentCanonicalized = new JsonCanonicalizer(expectedInitialDIDDocument.toJson()).getEncodedString();
		Map<String, Object> expectedInitialDIDDocumentMap = (Map<String, Object>) objectMapper.readValue(expectedInitialDIDDocumentCanonicalized, Map.class);

		assertEquals(expectedInitialDIDDocumentCanonicalized, initialDIDDocumentCanonicalized);
		assertEquals(expectedInitialDIDDocumentMap, initialDIDDocumentMap);
	}
}
