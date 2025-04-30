package uniresolver.driver.did.btc1.x1q20n602dgh7awm6akhgne0mjcmfpnjpc9jrqnrzuuexglrmklzm6u98hgvp;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import org.erdtman.jcs.JsonCanonicalizer;
import org.junit.jupiter.api.Test;
import uniresolver.driver.did.btc1.crud.read.Read;
import uniresolver.driver.did.btc1.crud.read.ResolveInitialDocument;
import uniresolver.driver.did.btc1.crud.read.ResolveTargetDocument;
import uniresolver.driver.did.btc1.syntax.DidBtc1IdentifierDecoding;
import uniresolver.driver.did.btc1.syntax.records.IdentifierComponents;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResolveTargetDocumentTest {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testResolveTargetDIDDocument() throws Exception {

		Map<String, Object> didDocumentMetadata = new HashMap<>();

		Read read = new Read(TestUtil.testBitcoinConnection(), TestUtil.testIpfsConnection());
		ResolveInitialDocument resolveInitialDocument = new ResolveInitialDocument(read, TestUtil.testBitcoinConnection(), TestUtil.testIpfsConnection());
		ResolveTargetDocument resolveTargetDocument = new ResolveTargetDocument(read, TestUtil.testBitcoinConnection(), TestUtil.testIpfsConnection());

		DID identifier = DID.fromString(TestUtil.readResourceString("did.txt"));
		Map<String, Object> resolutionOptions = TestUtil.readResourceJson("resolutionOptions.json");

		IdentifierComponents identifierComponents = DidBtc1IdentifierDecoding.didBtc1IdentifierDecoding(identifier);

		DIDDocument initialDIDDocument = resolveInitialDocument.resolveInitialDIDDocument(identifier, identifierComponents, resolutionOptions, didDocumentMetadata);

		DIDDocument targetDIDDocument = resolveTargetDocument.resolveTargetDocument(initialDIDDocument, resolutionOptions, identifierComponents.network(), didDocumentMetadata);
		String targetDIDDocumentCanonicalized = new JsonCanonicalizer(targetDIDDocument.toJson()).getEncodedString();
		Map<String, Object> targetDIDDocumentMap = (Map<String, Object>) objectMapper.readValue(targetDIDDocumentCanonicalized, Map.class);

		DIDDocument expectedTargetDIDDocument = DIDDocument.fromMap(TestUtil.readResourceJson("targetDidDocument.json"));
		String expectedTargetDIDDocumentCanonicalized = new JsonCanonicalizer(expectedTargetDIDDocument.toJson()).getEncodedString();
		Map<String, Object> expectedTargetDIDDocumentMap = (Map<String, Object>) objectMapper.readValue(expectedTargetDIDDocumentCanonicalized, Map.class);

		assertEquals(expectedTargetDIDDocumentCanonicalized, targetDIDDocumentCanonicalized);
		assertEquals(expectedTargetDIDDocumentMap, targetDIDDocumentMap);
	}
}
