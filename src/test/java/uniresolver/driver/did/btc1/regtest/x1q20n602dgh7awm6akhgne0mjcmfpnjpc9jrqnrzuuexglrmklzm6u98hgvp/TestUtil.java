package uniresolver.driver.did.btc1.regtest.x1q20n602dgh7awm6akhgne0mjcmfpnjpc9jrqnrzuuexglrmklzm6u98hgvp;

import com.fasterxml.jackson.databind.ObjectMapper;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.connections.bitcoin.BitcoinConnection;
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

    static BitcoinConnection testBitcoinConnection() {
        try {
            return BitcoindRPCBitcoinConnection.create(
                    Map.of(Network.regtest, URI.create("http://polaruser:polarpass@localhost:18443/").toURL())
            );
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    static IPFSConnection testIpfsConnection() {
        return IPFSConnection.create("/dns4/localhost/tcp/5001");
    }
}
