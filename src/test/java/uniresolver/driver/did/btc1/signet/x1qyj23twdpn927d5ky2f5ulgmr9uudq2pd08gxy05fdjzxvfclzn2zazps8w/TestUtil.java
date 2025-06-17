package uniresolver.driver.did.btc1.signet.x1qyj23twdpn927d5ky2f5ulgmr9uudq2pd08gxy05fdjzxvfclzn2zazps8w;

import com.fasterxml.jackson.databind.ObjectMapper;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.connections.bitcoin.BitcoinConnector;
import uniresolver.driver.did.btc1.connections.bitcoin.EsploraElectrsRESTBitcoinConnection;
import uniresolver.driver.did.btc1.connections.ipfs.IPFSConnection;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class TestUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static String readResourceString(String resourceName) {
        try {
            return Files.readString(Paths.get(TestUtil.class.getResource(resourceName).toURI()), StandardCharsets.UTF_8);
        } catch (URISyntaxException | IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    static Map<String, Object> readResourceJson(String resourceName) throws IOException {
        return (Map<String, Object>) objectMapper.readValue(readResourceString(resourceName), Map.class);
    }

    static BitcoinConnector testBitcoinConnections() {
        return BitcoinConnector.create(
                Map.of(
                        Network.signet, EsploraElectrsRESTBitcoinConnection.create(URI.create("https://mempool.space/signet/api/"))),
                Map.of(
                        Network.signet, "f61eee3b63a380a477a063af32b2bbc97c9ff9f01f2c4225e973988108000000")
            );
    }

    static IPFSConnection testIpfsConnection() {
        return IPFSConnection.create("/dns4/localhost/tcp/5001");
    }
}
