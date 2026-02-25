package uniresolver.driver.did.btcr2.regtest.k1;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;
import uniresolver.driver.did.btcr2.Network;
import uniresolver.driver.did.btcr2.connections.bitcoin.BitcoinConnector;
import uniresolver.driver.did.btcr2.connections.bitcoin.EsploraElectrsRESTBitcoinConnection;
import uniresolver.driver.did.btcr2.connections.ipfs.IPFSConnection;
import uniresolver.openapi.model.DidDocumentMetadata;
import uniresolver.openapi.model.DidResolutionMetadata;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class TestUtil {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public record Input(String did, Map<String, Object> sidecar) { }
    public record Output(DidResolutionMetadata didResolutionMetadata, DidDocumentMetadata didDocumentMetadata, DIDDocument didDocument) { }

    public static InputStreamReader readResourceString(String resourceName) {
        return new InputStreamReader(Objects.requireNonNull(TestUtil.class.getResourceAsStream(resourceName)), StandardCharsets.UTF_8);
    }

    public static Input readResourceInput(String resourceName) throws IOException {
        return objectMapper.readValue(readResourceString(resourceName), Input.class);
    }

    public static Output readResourceOutput(String resourceName) throws IOException {
        return objectMapper.readValue(readResourceString(resourceName), Output.class);
    }

    public static BitcoinConnector testBitcoinConnections() throws MalformedURLException {
        return BitcoinConnector.create(
                Map.of(
                        Network.regtest, EsploraElectrsRESTBitcoinConnection.create(URI.create("http://localhost:3000/"))),
                Map.of(
                        Network.regtest, "06226e46111a0b59caaf126043eb5bbf28c34f3a5e332a1fc7b2b73cf188910f")
        );
    }

    public static IPFSConnection testIpfsConnection() {
        return IPFSConnection.create("/dns4/localhost/tcp/5001");
    }
}
