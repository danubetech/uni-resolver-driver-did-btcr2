package uniresolver.driver.did.btcr2;

import com.fasterxml.jackson.databind.json.JsonMapper;
import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import org.erdtman.jcs.JsonCanonicalizer;
import org.junit.jupiter.api.Test;
import uniresolver.driver.did.btcr2.crud.resolve.Resolve;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractResolveTest {

	private static final JsonMapper jsonMapper = JsonMapper.builder().build();

	private final String relativePath;

	protected AbstractResolveTest(String relativePath) {
		this.relativePath = relativePath;
	}

	@Test
	public void testResolve() throws Exception {

		TestUtil.Input input = TestUtil.readResourceInput(this.relativePath + "/" + "input.json");
		TestUtil.Output output = TestUtil.readResourceOutput(this.relativePath + "/" + "output.json");

		Resolve resolve = new Resolve(TestUtil.testBitcoinConnections(), TestUtil.testIpfsConnection());

		DID identifier = DID.fromString(input.did());
		Map<String, Object> resolutionOptions = input.resolutionOptions();

		uniresolver.result.ResolveResult resolveResult = resolve.resolve(identifier, resolutionOptions);

		DIDDocument resolvedDIDDocument = resolveResult.getDidDocument();
		String resolvedDIDDocumentCanonicalized = new JsonCanonicalizer(resolvedDIDDocument.toJson()).getEncodedString();
		Map<String, Object> resolvedDIDDocumentMap = (Map<String, Object>) jsonMapper.readValue(resolvedDIDDocumentCanonicalized, Map.class);

		DIDDocument expectedDIDDocument = output.didDocument();
		String expectedDIDDocumentCanonicalized = new JsonCanonicalizer(expectedDIDDocument.toJson()).getEncodedString();
		Map<String, Object> expectedDIDDocumentMap = (Map<String, Object>) jsonMapper.readValue(expectedDIDDocumentCanonicalized, Map.class);

		assertEquals(expectedDIDDocumentCanonicalized, resolvedDIDDocumentCanonicalized);
		assertEquals(expectedDIDDocumentMap, resolvedDIDDocumentMap);
	}
}
