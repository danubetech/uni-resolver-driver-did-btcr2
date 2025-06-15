package uniresolver.driver.did.btc1.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btc1.DidBtc1Driver;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.connections.bitcoin.*;
import uniresolver.driver.did.btc1.connections.ipfs.IPFSConnection;
import uniresolver.driver.did.btc1.crud.read.Read;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Configuration {

    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    public static Map<String, Object> getPropertiesFromEnvironment() {

        if (log.isDebugEnabled()) log.debug("Loading from environment: " + System.getenv());

        Map<String, Object> properties = new HashMap<>();

        try {

            String env_bitcoinConnections = System.getenv("uniresolver_driver_did_btc1_bitcoinConnections");
            String env_bitcoinConnectionsUrls = System.getenv("uniresolver_driver_did_btc1_bitcoinConnectionsUrls");
            String env_bitcoinConnectionsCerts = System.getenv("uniresolver_driver_did_btc1_bitcoinConnectionsCerts");
            String env_ipfs = System.getenv("uniresolver_driver_did_btc1_ipfs");

            if (env_bitcoinConnections != null) properties.put("bitcoinConnections", env_bitcoinConnections);
            if (env_bitcoinConnectionsUrls != null) properties.put("bitcoinConnectionsUrls", env_bitcoinConnectionsUrls);
            if (env_bitcoinConnectionsCerts != null) properties.put("bitcoinConnectionsCerts", env_bitcoinConnectionsCerts);
            if (env_ipfs != null) properties.put("ipfs", env_ipfs);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }

        return properties;
    }

    public static void configureFromProperties(DidBtc1Driver didBtc1Driver, Map<String, Object> properties) {

        if (log.isDebugEnabled()) log.debug("Configuring from properties: " + properties);

        try {

            // parse ipfs

            IPFSConnection ipfsConnection;

            String prop_ipfs = (String) properties.get("ipfs");

            ipfsConnection = IPFSConnection.create(prop_ipfs);

            // parse bitcoinConnection

            String prop_bitcoinConnectionsTypes = (String) properties.get("bitcoinConnections");
            String prop_bitcoinConnectionsUrls = (String) properties.get("bitcoinConnectionsUrls");
            String prop_bitcoinConnectionsCerts = (String) properties.get("bitcoinConnectionsCerts");

            List<String> bitcoinConnectionsTypesList = Arrays.asList(prop_bitcoinConnectionsTypes.split(";"));
            List<String> bitcoinConnectionsUrlsList = Arrays.asList(prop_bitcoinConnectionsUrls.split(";"));
            List<String> bitcoinConnectionsCertsList = Arrays.asList(prop_bitcoinConnectionsCerts.split(";"));

            Map<String, String> bitcoinConnectionsTypesMap = IntStream.range(0, bitcoinConnectionsTypesList.size() / 2).boxed().collect(Collectors.toMap(i -> bitcoinConnectionsTypesList.get(i * 2), i -> bitcoinConnectionsTypesList.get(i * 2 + 1)));
            Map<String, String> bitcoinConnectionsUrlsMap = IntStream.range(0, bitcoinConnectionsUrlsList.size() / 2).boxed().collect(Collectors.toMap(i -> bitcoinConnectionsUrlsList.get(i * 2), i -> bitcoinConnectionsUrlsList.get(i * 2 + 1)));
            Map<String, String> bitcoinConnectionsCertsMap = IntStream.range(0, bitcoinConnectionsCertsList.size() / 2).boxed().collect(Collectors.toMap(i -> bitcoinConnectionsCertsList.get(i * 2), i -> bitcoinConnectionsCertsList.get(i * 2 + 1)));

            Map<Network, BitcoinConnection> bitcoinConnectionsMap = new LinkedHashMap<>();

            for (Map.Entry<String, String> bitcoinConnectionsTypesEntry : bitcoinConnectionsTypesMap.entrySet()) {
                String networkName = bitcoinConnectionsTypesEntry.getKey();
                String bitcoinConnectionType = bitcoinConnectionsTypesEntry.getValue();
                String bitcoinConnectionUrl = bitcoinConnectionsUrlsMap.get(networkName);
                String bitcoinConnectionCert = bitcoinConnectionsCertsMap.get(networkName);
                BitcoinConnection bitcoinConnection = switch(bitcoinConnectionType) {
                    case "bitcoind" -> BitcoindRPCBitcoinConnection.create(URI.create(bitcoinConnectionUrl).toURL());
                    case "btcd" -> BTCDRPCBitcoinConnection.create(URI.create(bitcoinConnectionUrl).toURL());
                    case "bitcoinj" -> BitcoinjSPVBitcoinConnection.create(Network.valueOf(networkName));
                    case "blockcypherapi" -> BlockcypherAPIBitcoinConnection.create();
                    case "esploraelectrsrest" -> EsploraElectrsRESTBitcoinConnection.create(URI.create(bitcoinConnectionUrl));
                    default -> throw new IllegalArgumentException("Invalid bitcoinConnectionType: " + bitcoinConnectionType);
                };
                bitcoinConnectionsMap.put(Network.valueOf(networkName), bitcoinConnection);
            }

            BitcoinConnections bitcoinConnections = BitcoinConnections.create(bitcoinConnectionsMap);

            // configure

            didBtc1Driver.setRead(new Read(bitcoinConnections, ipfsConnection));
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}
