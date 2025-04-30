package uniresolver.driver.did.btc1.k1qgp6haekj3w5zgk56h92juynjl4ag4pt2p9wl4ajwu7yhklyp0ngcfskwzack;

import foundation.identity.did.DID;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.syntax.DidBtc1IdentifierDecoding;
import uniresolver.driver.did.btc1.syntax.records.IdentifierComponents;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DidBtc1IdentifierDecodingTest {

	@Test
	public void testDidBtc1IdentifierDecoding() throws Exception {

		DID identifier = DID.fromString(TestUtil.readResourceString("did.txt"));
		IdentifierComponents identifierComponents = DidBtc1IdentifierDecoding.didBtc1IdentifierDecoding(identifier);

		Map<String, Object> expectedIdentifierComponents = TestUtil.readResourceJson("identifierComponents.json");
		String expectedIdType = (String) expectedIdentifierComponents.get("idType");
		Integer expectedVersion = (Integer) expectedIdentifierComponents.get("version");
		Network expectedNetwork = Network.valueOf((String) expectedIdentifierComponents.get("network"));
		byte[] expectedGenesisBytes = Base64.decodeBase64((String) expectedIdentifierComponents.get("genesisBytes"));

		assertEquals(expectedIdType, identifierComponents.idType());
		assertEquals(expectedVersion, identifierComponents.version());
		assertEquals(expectedNetwork, identifierComponents.network());
		assertArrayEquals(expectedGenesisBytes, identifierComponents.genesisBytes());
	}
}
