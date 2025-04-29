package uniresolver.driver.did.btc1.x1qttdgkmat4vykwuurkcfagy5fk4eth2fe4tfgvp42nzudclzpzsxkunlyr5;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import org.erdtman.jcs.JsonCanonicalizer;
import org.junit.jupiter.api.Test;
import uniresolver.driver.did.btc1.crud.read.Read;
import uniresolver.driver.did.btc1.crud.read.ResolveInitialDocument;
import uniresolver.driver.did.btc1.syntax.DidBtc1IdentifierDecoding;
import uniresolver.driver.did.btc1.syntax.records.IdentifierComponents;
import uniresolver.driver.did.btc1.util.JSONUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResolveInitialDocumentTest {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testResolveInitialDIDDocument() throws Exception {

		Map<String, Object> didDocumentMetadata = new HashMap<>();

		Read read = new Read(TestUtil.testBitcoinConnection(), TestUtil.testIpfsConnection());
		ResolveInitialDocument resolveInitialDocument = new ResolveInitialDocument(read, TestUtil.testBitcoinConnection(), TestUtil.testIpfsConnection());

		DID identifier = DID.fromString(TestUtil.readResourceString("did.txt"));
		Map<String, Object> resolutionOptions = TestUtil.readResourceJson("resolutionOptions.json");

		IdentifierComponents identifierComponents = DidBtc1IdentifierDecoding.didBtc1IdentifierDecoding(identifier);

		DIDDocument initialDIDDocument = resolveInitialDocument.resolveInitialDIDDocument(identifier, identifierComponents, resolutionOptions, didDocumentMetadata);
		String initialDIDDocumentCanonicalized = new JsonCanonicalizer(initialDIDDocument.toJson()).getEncodedString();
		Map<String, Object> initialDIDDocumentMap = (Map<String, Object>) objectMapper.readValue(initialDIDDocumentCanonicalized, Map.class);

		DIDDocument expectedInitialDIDDocument = DIDDocument.fromMap(TestUtil.readResourceJson("initialDidDoc.json"));
		String expectedInitialDIDDocumentCanonicalized = new JsonCanonicalizer(expectedInitialDIDDocument.toJson()).getEncodedString();
		Map<String, Object> expectedInitialDIDDocumentMap = (Map<String, Object>) objectMapper.readValue(expectedInitialDIDDocumentCanonicalized, Map.class);

		Map<String, Object> intermediateDocumentRepresentationMap = JSONUtil.jsonToMap((String) didDocumentMetadata.get("intermediateDocumentRepresentation"));
		Map<String, Object> expectedIntermediateDocumentRepresentationMap = TestUtil.readResourceJson("intermediateDidDoc.json");

		assertEquals(expectedInitialDIDDocumentCanonicalized, initialDIDDocumentCanonicalized);
		assertEquals(expectedInitialDIDDocumentMap, initialDIDDocumentMap);
		assertEquals(expectedIntermediateDocumentRepresentationMap, intermediateDocumentRepresentationMap);
	}
}
