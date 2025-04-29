package uniresolver.driver.did.btc1.k1qgpzs6takyvuhv3dy8epaqhwee6eamxttprpn4k48ft4xyvw5sp3mvqqavunt;

import foundation.identity.did.DID;
import org.apache.commons.codec.binary.Hex;
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
		byte[] expectedGenesisBytes = Hex.decodeHex((String) expectedIdentifierComponents.get("hexGenesisBytes"));

		assertEquals(expectedIdType, identifierComponents.idType());
		assertEquals(expectedVersion, identifierComponents.version());
		assertEquals(expectedNetwork, identifierComponents.network());
		assertArrayEquals(expectedGenesisBytes, identifierComponents.genesisBytes());
	}
}
