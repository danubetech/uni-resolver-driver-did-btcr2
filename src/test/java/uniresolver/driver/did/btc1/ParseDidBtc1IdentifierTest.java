package uniresolver.driver.did.btc1;

import foundation.identity.did.DID;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;
import uniresolver.driver.did.btc1.crud.read.ParseDidBtc1Identifier;
import uniresolver.driver.did.btc1.crud.read.records.IdentifierComponents;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseDidBtc1IdentifierTest {

	@Test
	public void testParseDidBtc1Identifier() throws Exception {

		Map<String, Object> didDocumentMetadata = new HashMap<>();

		DID identifier = DID.fromString(TestUtil.readResourceString("did.txt"));
		IdentifierComponents identifierComponents = ParseDidBtc1Identifier.parseDidBtc1Identifier(identifier, didDocumentMetadata);

		Map<String, Object> expectedIdentifierComponents = TestUtil.readResourceJson("identifierComponents.json");
		Integer expectedVersion = (Integer) expectedIdentifierComponents.get("version");
		Network expectedNetwork = Network.valueOf((String) expectedIdentifierComponents.get("network"));
		String expectedHrp = (String) expectedIdentifierComponents.get("hrp");
		byte[] expectedGenesisBytes = Hex.decodeHex((String) expectedIdentifierComponents.get("hexGenesisBytes"));

		assertEquals(expectedVersion, identifierComponents.version());
		assertEquals(expectedNetwork, identifierComponents.network());
		assertEquals(expectedHrp, identifierComponents.hrp());
		assertArrayEquals(expectedGenesisBytes, identifierComponents.genesisBytes());
	}
}
