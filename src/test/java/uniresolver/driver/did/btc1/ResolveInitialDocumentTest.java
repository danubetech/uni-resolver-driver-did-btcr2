package uniresolver.driver.did.btc1;

import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import org.junit.jupiter.api.Test;
import uniresolver.driver.did.btc1.crud.read.ParseDidBtc1Identifier;
import uniresolver.driver.did.btc1.crud.read.ResolveInitialDocument;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResolveInitialDocumentTest {

	@Test
	public void testResolve() throws Exception {

		ResolveInitialDocument resolveInitialDocument = new ResolveInitialDocument(null);

		DID identifier = DID.fromString(TestUtil.readResourceString("did.txt"));
		ParseDidBtc1Identifier.IdentifierComponents identifierComponents = ParseDidBtc1Identifier.parseDidBtc1Identifier(identifier);
		Map<String, Object> resolutionOptions = new HashMap<>();
		DIDDocument initialDIDDocument = resolveInitialDocument.resolveInitialDIDDocument(identifier, identifierComponents, resolutionOptions);

		DIDDocument expectedInitialDIDDocument = DIDDocument.fromMap(TestUtil.readResourceJson("initialDidDocument.json"));

		assertEquals(expectedInitialDIDDocument, initialDIDDocument);
	}
}
