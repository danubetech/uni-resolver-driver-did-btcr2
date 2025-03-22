package uniresolver.driver.did.btc1;

import foundation.identity.did.DID;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;
import uniresolver.driver.did.btc1.crud.read.ParseDidBtc1Identifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseDidBtc1IdentifierTest {

	@Test
	public void testParseDidBtc1Identifier() throws Exception {

		ParseDidBtc1Identifier.IdentifierComponents identifierComponents = ParseDidBtc1Identifier.parseDidBtc1Identifier(DID.fromString("did:btc1:regtest:k1qvadgpl5qfuz6emq7c8sqw28z0r0gzvyra3je3pp2cuk83uqnnyvckvw8cf"));

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
