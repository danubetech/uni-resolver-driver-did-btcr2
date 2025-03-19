package uniresolver.driver.did.btc1;

import foundation.identity.did.DID;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IdentifierComponentsTest {

	@Test
	public void testIdentifierComponents() throws Exception {

		IdentifierComponents identifierComponents = IdentifierComponents.parse(DID.fromString("did:btc1:regtest:k1qvadgpl5qfuz6emq7c8sqw28z0r0gzvyra3je3pp2cuk83uqnnyvckvw8cf"));

		Map<String, Object> expectedIdentifierComponents = TestUtil.readResourceJson("identifierComponents.json");
		Integer expectedVersion = (Integer) expectedIdentifierComponents.get("version");
		Network expectedNetwork = Network.valueOf((String) expectedIdentifierComponents.get("network"));
		String expectedHrp = (String) expectedIdentifierComponents.get("hrp");
		byte[] expectedGenesisBytes = Hex.decodeHex((String) expectedIdentifierComponents.get("hexGenesisBytes"));

		assertEquals(expectedVersion, identifierComponents.getVersion());
		assertEquals(expectedNetwork, identifierComponents.getNetwork());
		assertEquals(expectedHrp, identifierComponents.getHrp());
		assertArrayEquals(expectedGenesisBytes, identifierComponents.getGenesisBytes());
	}
}
