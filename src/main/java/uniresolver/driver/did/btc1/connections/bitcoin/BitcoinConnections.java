package uniresolver.driver.did.btc1.connections.bitcoin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btc1.Network;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

import java.net.URL;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BitcoinConnections {

	private static final Logger log = LoggerFactory.getLogger(BitcoinConnections.class);

	private Map<Network, BitcoinConnection> bitcoinConnections;

	private BitcoinConnections(Map<Network, BitcoinConnection> bitcoinConnections) {
		if (log.isDebugEnabled()) log.debug("Creating BitcoinConnections: " + bitcoinConnections);
		this.bitcoinConnections = bitcoinConnections;
	}

	public static BitcoinConnections create(Map<Network, URL> rpcUrls) {
		if (log.isDebugEnabled()) log.debug("Creating BitcoindRPCBitcoinConnection: " + rpcUrls);
		return new BitcoinConnections(rpcUrls.entrySet()
				.stream()
				.map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), BitcoindRPCBitcoinConnection.create(e.getValue())))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	public static BitcoinConnections create() {
		if (log.isDebugEnabled()) log.debug("Creating BitcoindRPCBitcoinConnection");
		return create(Map.of(
				Network.bitcoin, BitcoinJSONRPCClient.DEFAULT_JSONRPC_URL,
				Network.regtest, BitcoinJSONRPCClient.DEFAULT_JSONRPC_REGTEST_URL,
				Network.testnet3, BitcoinJSONRPCClient.DEFAULT_JSONRPC_TESTNET_URL,
				Network.testnet4, BitcoinJSONRPCClient.DEFAULT_JSONRPC_TESTNET_URL)
		);
	}

	public BitcoinConnection getBitcoinConnection(Network network) {
		BitcoinConnection bitcoinConnection = this.bitcoinConnections.get(network);
		if (bitcoinConnection == null) throw new IllegalArgumentException("Unknown network: " + network);
		if (log.isDebugEnabled()) log.debug("BitcoinConnection for " + network + ": " + bitcoinConnection.getClass().getName());
		return bitcoinConnection;
	}
}
