package uniresolver.driver.did.btc1.connections.bitcoin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btc1.Network;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

import java.util.Map;

public class BitcoinConnections {

	private static final Logger log = LoggerFactory.getLogger(BitcoinConnections.class);

	private Map<Network, BitcoinConnection> bitcoinConnections;

	private BitcoinConnections(Map<Network, BitcoinConnection> bitcoinConnections) {
		if (log.isDebugEnabled()) log.debug("Creating BitcoinConnections: " + bitcoinConnections);
		this.bitcoinConnections = bitcoinConnections;
	}

	public static BitcoinConnections create(Map<Network, BitcoinConnection> bitcoinConnections) {
		return new BitcoinConnections(bitcoinConnections);
	}

	public static BitcoinConnections create() {
		return create(Map.of(
				Network.bitcoin, BitcoindRPCBitcoinConnection.create(BitcoinJSONRPCClient.DEFAULT_JSONRPC_URL),
				Network.regtest, BitcoindRPCBitcoinConnection.create(BitcoinJSONRPCClient.DEFAULT_JSONRPC_REGTEST_URL),
				Network.testnet3, BitcoindRPCBitcoinConnection.create(BitcoinJSONRPCClient.DEFAULT_JSONRPC_TESTNET_URL),
				Network.testnet4, BitcoindRPCBitcoinConnection.create(BitcoinJSONRPCClient.DEFAULT_JSONRPC_TESTNET_URL))
		);
	}

	public BitcoinConnection getBitcoinConnection(Network network) {
		BitcoinConnection bitcoinConnection = this.bitcoinConnections.get(network);
		if (bitcoinConnection == null) throw new IllegalArgumentException("Unknown network: " + network);
		if (log.isDebugEnabled()) log.debug("BitcoinConnection for " + network + ": " + bitcoinConnection.getClass().getName());
		return bitcoinConnection;
	}
}
