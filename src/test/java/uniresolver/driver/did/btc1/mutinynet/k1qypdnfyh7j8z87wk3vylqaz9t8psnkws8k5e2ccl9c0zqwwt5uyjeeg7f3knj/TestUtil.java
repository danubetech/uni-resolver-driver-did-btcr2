package uniresolver.driver.did.btc1.mutinynet.k1qypdnfyh7j8z87wk3vylqaz9t8psnkws8k5e2ccl9c0zqwwt5uyjeeg7f3knj;

import com.fasterxml.jackson.databind.ObjectMapper;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.connections.bitcoin.BitcoinConnection;
import uniresolver.driver.did.btc1.connections.bitcoin.BitcoinConnections;
import uniresolver.driver.did.btc1.connections.bitcoin.BitcoindRPCBitcoinConnection;
import uniresolver.driver.did.btc1.connections.ipfs.IPFSConnection;

import java.io.IOException;
import java.net.MalformedURLException;
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

    static BitcoinConnections testBitcoinConnections() {
        try {
            return BitcoinConnections.create(Map.of(
                    Network.signet, BitcoindRPCBitcoinConnection.create(URI.create("http://mbjcimllwl.b.voltageapp.io:38332/").toURL()),
                    Network.regtest, BitcoindRPCBitcoinConnection.create(URI.create("http://polaruser:polarpass@localhost:18443/").toURL())
            ));
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    static IPFSConnection testIpfsConnection() {
        return IPFSConnection.create("/dns4/localhost/tcp/5001");
    }
}
