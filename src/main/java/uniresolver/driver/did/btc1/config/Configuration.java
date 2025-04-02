package uniresolver.driver.did.btc1.config;

import io.ipfs.api.IPFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btc1.DidBtc1Driver;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.bitcoinconnection.BTCDRPCBitcoinConnection;
import uniresolver.driver.did.btc1.bitcoinconnection.BitcoinConnection;
import uniresolver.driver.did.btc1.bitcoinconnection.BitcoindRPCBitcoinConnection;
import uniresolver.driver.did.btc1.crud.read.Read;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Configuration {

    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    public static Map<String, Object> getPropertiesFromEnvironment() {

        if (log.isDebugEnabled()) log.debug("Loading from environment: " + System.getenv());

        Map<String, Object> properties = new HashMap<>();

        try {

            String env_bitcoinConnection = System.getenv("uniresolver_driver_did_btc1_bitcoinConnection");
            String env_rpcUrlMainnet = System.getenv("uniresolver_driver_did_btc1_rpcUrlMainnet");
            String env_rpcUrlTestnet = System.getenv("uniresolver_driver_did_btc1_rpcUrlTestnet");
            String env_rpcUrlSignet = System.getenv("uniresolver_driver_did_btc1_rpcUrlSignet");
            String env_rpcUrlRegtest = System.getenv("uniresolver_driver_did_btc1_rpcUrlRegtest");
            String env_rpcCertMainnet = System.getenv("uniresolver_driver_did_btc1_rpcCertMainnet");
            String env_rpcCertTestnet = System.getenv("uniresolver_driver_did_btc1_rpcCertTestnet");
            String env_rpcCertSignet = System.getenv("uniresolver_driver_did_btc1_rpcCertSignet");
            String env_rpcCertRegtest = System.getenv("uniresolver_driver_did_btc1_rpcCertRegtest");
            String env_ipfs = System.getenv("uniresolver_driver_did_btc1_ipfs");

            if (env_bitcoinConnection != null) properties.put("bitcoinConnection", env_bitcoinConnection);
            if (env_rpcUrlMainnet != null) properties.put("rpcUrlMainnet", env_rpcUrlMainnet);
            if (env_rpcUrlTestnet != null) properties.put("rpcUrlTestnet", env_rpcUrlTestnet);
            if (env_rpcUrlSignet != null) properties.put("rpcUrlSignet", env_rpcUrlSignet);
            if (env_rpcUrlRegtest != null) properties.put("rpcUrlRegtest", env_rpcUrlRegtest);
            if (env_rpcCertMainnet != null) properties.put("rpcCertMainnet", env_rpcCertMainnet);
            if (env_rpcCertTestnet != null) properties.put("rpcCertTestnet", env_rpcCertTestnet);
            if (env_rpcCertSignet != null) properties.put("rpcCertSignet", env_rpcCertSignet);
            if (env_rpcCertRegtest != null) properties.put("rpcCertRegtest", env_rpcCertRegtest);
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

            String prop_ipfs = (String) properties.get("ipfs");
            IPFS ipfs = new IPFS(prop_ipfs);

            // parse bitcoinConnection

            String prop_bitcoinConnection = (String) properties.get("bitcoinConnection");

            String prop_rpcUrlMainnet = (String) properties.get("rpcUrlMainnet");
            String prop_rpcUrlTestnet = (String) properties.get("rpcUrlTestnet");
            String prop_rpcUrlSignet = (String) properties.get("rpcUrlSignet");
            String prop_rpcUrlRegtest = (String) properties.get("rpcUrlRegtest");
            String prop_rpcCertTestnet = (String) properties.get("rpcCertTestnet");
            String prop_rpcCertMainnet = (String) properties.get("rpcCertMainnet");
            String prop_rpcCertSignet = (String) properties.get("rpcCertSignet");
            String prop_rpcCertRegtest = (String) properties.get("rpcCertRegtest");

            Map<Network, URL> rpcUrls = new HashMap<>();

            if (prop_rpcUrlMainnet != null && ! prop_rpcUrlMainnet.isBlank()) {
                rpcUrls.put(Network.mainnet, URI.create(prop_rpcUrlMainnet).toURL());
            }
            if (prop_rpcUrlTestnet != null && ! prop_rpcUrlTestnet.isBlank()) {
                rpcUrls.put(Network.testnet, URI.create(prop_rpcUrlTestnet).toURL());
            }
            if (prop_rpcUrlSignet != null && ! prop_rpcUrlSignet.isBlank()) {
                rpcUrls.put(Network.signet, URI.create(prop_rpcUrlSignet).toURL());
            }
            if (prop_rpcUrlRegtest != null && ! prop_rpcUrlRegtest.isBlank()) {
                rpcUrls.put(Network.regtest, URI.create(prop_rpcUrlRegtest).toURL());
            }

            BitcoinConnection bitcoinConnection;

            if ("bitcoind".equalsIgnoreCase(prop_bitcoinConnection)) {
                bitcoinConnection = BitcoindRPCBitcoinConnection.create(rpcUrls);
            } else if ("btcd".equalsIgnoreCase(prop_bitcoinConnection)) {
                bitcoinConnection = BTCDRPCBitcoinConnection.create(rpcUrls);
            } else if ("bitcoinj".equalsIgnoreCase(prop_bitcoinConnection)) {
                throw new RuntimeException("bitcoinj is not implemented yet");
            } else if ("blockcypherapi".equalsIgnoreCase(prop_bitcoinConnection)) {
                throw new RuntimeException("h is not implemented yet");
            } else {
                throw new IllegalArgumentException("Invalid bitcoinConnection: " + prop_bitcoinConnection);
            }

            didBtc1Driver.setRead(new Read(bitcoinConnection, ipfs));
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}
