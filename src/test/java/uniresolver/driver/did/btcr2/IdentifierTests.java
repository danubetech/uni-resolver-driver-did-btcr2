package uniresolver.driver.did.btcr2;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DID;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;
import uniresolver.driver.did.btcr2.syntax.DidBtcr2IdentifierDecoding;
import uniresolver.driver.did.btcr2.syntax.DidBtcr2IdentifierEncoding;
import uniresolver.driver.did.btcr2.syntax.records.IdentifierComponents;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IdentifierTests {

	private static final String IDENTIFIER_TESTS =
	"""
		[
			{
				"did": "did:btcr2:k1qqptaz4ydc2q8qjgch9kl46y48ccdhjyqdzxxjmmaupwsv9sut5ssfsm0s3dn",
				"identifierComponents": {
					"idType": "key",
					"version": 1,
					"network": "bitcoin",
					"genesisBytes": "02be8aa46e14038248c5cb6fd744a9f186de440344634b7bef02e830b0e2e90826"
				}
			},
			{
				"did": "did:btcr2:k1qvpxlu8m9l4jw9czmthaf7zf6e96pfv2ak05utxmwhrv0zrtgrgdrwggpepd9",
				"identifierComponents": {
					"idType": "key",
					"version": 1,
					"network": "testnet3",
					"genesisBytes": "026ff0fb2feb271702daefd4f849d64ba0a58aed9f4e2cdb75c6c7886b40d0d1b9"
				}
			},
			{
				"did": "did:btcr2:k1qypvksjk8vfxpp0pl6jzwvc4sw7knmv8q4l2j5j2vgsjwfrfer2vqqqgrc3cx",
				"identifierComponents": {
					"idType": "key",
					"version": 1,
					"network": "signet",
					"genesisBytes": "02cb42563b126085e1fea427331583bd69ed87057ea9524a6221272469c8d4c000"
				}
			},
			{
				"did": "did:btcr2:k1psppl550jkrj9l2caef72m98k3z2ytvfkjv9uftv3htkn8n54979cwg5ht5py",
				"identifierComponents": {
					"idType": "key",
					"version": 1,
					"network": "userdefinedC",
					"genesisBytes": "021fd28f958722fd58ee53e56ca7b444a22d89b4985e256c8dd7699e74a97c5c39"
				}
			},
			{
				"did": "did:btcr2:x1qzlqmvawa6ya5fx4qyf27a85p34z07z060h352qxgl65fr6d4ugmzm5tzxq",
				"identifierComponents": {
					"idType": "external",
					"version": 1,
					"network": "bitcoin",
					"genesisBytes": "be0db3aeee89da24d50112af74f40c6a27f84fd3ef1a280647f5448f4daf11b1"
				}
			},
			{
				"did": "did:btcr2:x1q2lqmvawa6ya5fx4qyf27a85p34z07z060h352qxgl65fr6d4ugmzxrg4q8",
				"identifierComponents": {
					"idType": "external",
					"version": 1,
					"network": "regtest",
					"genesisBytes": "be0db3aeee89da24d50112af74f40c6a27f84fd3ef1a280647f5448f4daf11b1"
				}
			},
			{
				"did": "did:btcr2:x1qjlqmvawa6ya5fx4qyf27a85p34z07z060h352qxgl65fr6d4ugmzgnd92w",
				"identifierComponents": {
					"idType": "external",
					"version": 1,
					"network": "testnet4",
					"genesisBytes": "be0db3aeee89da24d50112af74f40c6a27f84fd3ef1a280647f5448f4daf11b1"
				}
			}
		]
	""";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testIdentifiers() throws Exception {

		List<Map<String, Object>> testIdentifiers = objectMapper.readValue(new StringReader(IDENTIFIER_TESTS), List.class);

		for (Map<String, Object> testIdentifier : testIdentifiers) {
			DID did = DID.fromString((String) testIdentifier.get("did"));
			Map<String, Object> identifierComponents = (Map<String, Object>) testIdentifier.get("identifierComponents");
			String idType = (String) identifierComponents.get("idType");
			Integer version = (Integer) identifierComponents.get("version");
			Network network = Network.valueOf(identifierComponents.get("network"));
			byte[] genesisBytes = Hex.decodeHex((String) identifierComponents.get("genesisBytes"));

			IdentifierComponents actualIdentifierComponents = DidBtcr2IdentifierDecoding.didBtcr2IdentifierDecoding(did);
			assertEquals(idType, actualIdentifierComponents.idType());
			assertEquals(version, actualIdentifierComponents.version());
			assertEquals(network, actualIdentifierComponents.network());
			assertArrayEquals(genesisBytes, actualIdentifierComponents.genesisBytes());

			DID actualDid = DidBtcr2IdentifierEncoding.didBtcr2IdentifierEncoding(idType, version, network, genesisBytes);
			assertEquals(did, actualDid);
		}
	}
}
