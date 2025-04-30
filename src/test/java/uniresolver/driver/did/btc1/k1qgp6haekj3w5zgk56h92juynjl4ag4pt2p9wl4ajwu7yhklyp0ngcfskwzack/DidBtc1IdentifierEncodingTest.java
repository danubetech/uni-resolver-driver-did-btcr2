package uniresolver.driver.did.btc1.k1qgp6haekj3w5zgk56h92juynjl4ag4pt2p9wl4ajwu7yhklyp0ngcfskwzack;

import foundation.identity.did.DID;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.syntax.DidBtc1IdentifierEncoding;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DidBtc1IdentifierEncodingTest {

	@Test
	public void testDidBtc1IdentifierEncoding() throws Exception {

		Map<String, Object> expectedIdentifierComponents = TestUtil.readResourceJson("identifierComponents.json");
		String idType = (String) expectedIdentifierComponents.get("idType");
		Integer version = (Integer) expectedIdentifierComponents.get("version");
		Network network = Network.valueOf((String) expectedIdentifierComponents.get("network"));
		byte[] genesisBytes = Base64.decodeBase64((String) expectedIdentifierComponents.get("genesisBytes"));
		DID identifier = DidBtc1IdentifierEncoding.didBtc1IdentifierEncoding(idType, version, network, genesisBytes);

		DID expectedIdentifier = DID.fromString(TestUtil.readResourceString("did.txt"));

		assertEquals(expectedIdentifier, identifier);
	}
}
