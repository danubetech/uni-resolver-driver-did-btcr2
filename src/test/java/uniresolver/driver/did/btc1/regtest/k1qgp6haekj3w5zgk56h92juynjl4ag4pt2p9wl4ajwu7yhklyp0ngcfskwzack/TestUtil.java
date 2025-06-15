package uniresolver.driver.did.btc1.regtest.k1qgp6haekj3w5zgk56h92juynjl4ag4pt2p9wl4ajwu7yhklyp0ngcfskwzack;

import com.fasterxml.jackson.databind.ObjectMapper;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.connections.bitcoin.BitcoinConnector;
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

    static BitcoinConnector testBitcoinConnections() throws MalformedURLException {
        return BitcoinConnector.create(
                Map.of(
                        Network.regtest, BitcoindRPCBitcoinConnection.create(URI.create("http://polaruser:polarpass@localhost:18443/").toURL())),
                Map.of(
                        Network.regtest, "06226e46111a0b59caaf126043eb5bbf28c34f3a5e332a1fc7b2b73cf188910f")
        );
    }

    static IPFSConnection testIpfsConnection() {
        return IPFSConnection.create("/dns4/localhost/tcp/5001");
    }
}
