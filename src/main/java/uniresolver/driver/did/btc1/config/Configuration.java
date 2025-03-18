package uniresolver.driver.did.btc1.config;

import info.weboftrust.btctxlookup.Chain;
import info.weboftrust.btctxlookup.bitcoinconnection.BTCDRPCBitcoinConnection;
import info.weboftrust.btctxlookup.bitcoinconnection.BitcoindRPCBitcoinConnection;
import info.weboftrust.btctxlookup.bitcoinconnection.BlockcypherAPIBitcoinConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btc1.DidBtc1Driver;
import uniresolver.driver.did.btc1.tls.Tls;

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
            String env_rpcCertMainnet = System.getenv("uniresolver_driver_did_btc1_rpcCertMainnet");
            String env_rpcCertTestnet = System.getenv("uniresolver_driver_did_btc1_rpcCertTestnet");

            if (env_bitcoinConnection != null) properties.put("bitcoinConnection", env_bitcoinConnection);
            if (env_rpcUrlMainnet != null) properties.put("rpcUrlMainnet", env_rpcUrlMainnet);
            if (env_rpcUrlTestnet != null) properties.put("rpcUrlTestnet", env_rpcUrlTestnet);
            if (env_rpcCertMainnet != null) properties.put("rpcCertMainnet", env_rpcCertMainnet);
            if (env_rpcCertTestnet != null) properties.put("rpcCertTestnet", env_rpcCertTestnet);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }

        return properties;
    }

    public static void configureFromProperties(DidBtc1Driver didBtc1Driver, Map<String, Object> properties) {

        if (log.isDebugEnabled()) log.debug("Configuring from properties: " + properties);

        try {

            // parse bitcoinConnection

            String prop_bitcoinConnection = (String) properties.get("bitcoinConnection");

            String prop_rpcUrlMainnet = (String) properties.get("rpcUrlMainnet");
            String prop_rpcUrlTestnet = (String) properties.get("rpcUrlTestnet");
            String prop_rpcCertTestnet = (String) properties.get("rpcCertTestnet");
            String prop_rpcCertMainnet = (String) properties.get("rpcCertMainnet");

            if ("bitcoind".equalsIgnoreCase(prop_bitcoinConnection)) {
                if (prop_rpcUrlMainnet != null && !prop_rpcUrlMainnet.isBlank()) {
                    didBtc1Driver.setBitcoinConnectionMainnet(new BitcoindRPCBitcoinConnection(prop_rpcUrlMainnet, Chain.MAINNET));
                }
                if (prop_rpcUrlTestnet != null && !prop_rpcUrlTestnet.isBlank()) {
                    didBtc1Driver.setBitcoinConnectionTestnet(new BitcoindRPCBitcoinConnection(prop_rpcUrlTestnet, Chain.TESTNET));
                }
            } else if ("btcd".equalsIgnoreCase(prop_bitcoinConnection)) {

                if (prop_rpcUrlMainnet != null && !prop_rpcUrlMainnet.isBlank()) {
                    BTCDRPCBitcoinConnection btcdrpcBitcoinConnection = new BTCDRPCBitcoinConnection(prop_rpcUrlMainnet, Chain.MAINNET);
                    if (prop_rpcCertMainnet != null && !prop_rpcCertMainnet.isBlank()) {
                        btcdrpcBitcoinConnection.getBitcoindRpcClient().setSslSocketFactory(Tls.getSslSocketFactory(prop_rpcCertMainnet));
                    }
                    didBtc1Driver.setBitcoinConnectionMainnet(btcdrpcBitcoinConnection);
                }
                if (prop_rpcUrlTestnet != null && !prop_rpcUrlTestnet.isBlank()) {
                    BTCDRPCBitcoinConnection btcdrpcBitcoinConnection = new BTCDRPCBitcoinConnection(prop_rpcUrlTestnet, Chain.TESTNET);
                    if (prop_rpcCertTestnet != null && !prop_rpcCertTestnet.isBlank()) {
                        btcdrpcBitcoinConnection.getBitcoindRpcClient().setSslSocketFactory(Tls.getSslSocketFactory(prop_rpcCertTestnet));
                    }
                    didBtc1Driver.setBitcoinConnectionTestnet(btcdrpcBitcoinConnection);
                }
            } else if ("bitcoinj".equalsIgnoreCase(prop_bitcoinConnection)) {
                throw new RuntimeException("bitcoinj is not implemented yet");
            } else if ("blockcypherapi".equalsIgnoreCase(prop_bitcoinConnection)) {
                didBtc1Driver.setBitcoinConnectionMainnet(new BlockcypherAPIBitcoinConnection());
                didBtc1Driver.setBitcoinConnectionTestnet(new BlockcypherAPIBitcoinConnection());
            } else {
                throw new IllegalArgumentException("Invalid bitcoinConnection: " + prop_bitcoinConnection);
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}
