package uniresolver.driver.did.btcr2;

import com.danubetech.btc.connection.BitcoinConnector;
import com.danubetech.btc.connection.Network;
import com.danubetech.btc.connection.impl.EsploraElectrsRESTBitcoinConnection;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import foundation.identity.did.DIDDocument;
import uniresolver.driver.did.btcr2.ipfs.IPFSConnection;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class TestUtil {

    private static final JsonMapper jsonMapper = JsonMapper.builder()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

    public record Input(String did, Map<String, Object> resolutionOptions) { }
    public record Output(Map<String, Object> didResolutionMetadata, Map<String, Object> didDocumentMetadata, DIDDocument didDocument) { }

    public static InputStreamReader readResourceString(String resourceName) {
        return new InputStreamReader(Objects.requireNonNull(TestUtil.class.getResourceAsStream(resourceName)), StandardCharsets.UTF_8);
    }

    public static Input readResourceInput(String resourceName) throws IOException {
        return jsonMapper.readValue(readResourceString(resourceName), Input.class);
    }

    public static Output readResourceOutput(String resourceName) throws IOException {
        return jsonMapper.readValue(readResourceString(resourceName), Output.class);
    }

    public static BitcoinConnector testBitcoinConnections() throws MalformedURLException {
        return BitcoinConnector.create(
                Map.of(
                        Network.regtest, EsploraElectrsRESTBitcoinConnection.create(Network.regtest, URI.create("http://localhost:3000/"))),
                Map.of(
                        Network.regtest, "06226e46111a0b59caaf126043eb5bbf28c34f3a5e332a1fc7b2b73cf188910f")
        );
    }

    public static IPFSConnection testIpfsConnection() {
        return IPFSConnection.create("/dns4/localhost/tcp/5001");
    }
}
