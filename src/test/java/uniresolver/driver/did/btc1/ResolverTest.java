package uniresolver.driver.did.btc1;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.runtime.ObjectMethods;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResolverTest {

	@Test
	public void testResolve() throws Exception {

		Resolver resolver = new Resolver(null);

		DID identifier = DID.fromString(TestUtil.readResourceString("did.txt"));
		IdentifierComponents identifierComponents = IdentifierComponents.parse(identifier);
		Map<String, Object> resolutionOptions = new HashMap<>();
		DIDDocument initialDIDDocument = resolver.resolveInitialDIDDocument(identifier, identifierComponents, resolutionOptions);

		DIDDocument expectedInitialDIDDocument = DIDDocument.fromMap(TestUtil.readResourceJson("initialDidDocument.json"));

		assertEquals(expectedInitialDIDDocument, initialDIDDocument);
	}
}
