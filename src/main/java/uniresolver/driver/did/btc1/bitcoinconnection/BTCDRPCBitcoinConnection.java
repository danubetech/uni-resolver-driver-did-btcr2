package uniresolver.driver.did.btc1.bitcoinconnection;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.bitcoinconnection.records.Block;
import uniresolver.driver.did.btc1.bitcoinconnection.records.Tx;
import uniresolver.driver.did.btc1.bitcoinconnection.records.TxIn;
import uniresolver.driver.did.btc1.bitcoinconnection.records.TxOut;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.net.URL;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BTCDRPCBitcoinConnection extends AbstractBitcoinConnection implements BitcoinConnection {

	private static final Logger log = LoggerFactory.getLogger(BTCDRPCBitcoinConnection.class);

	private static final ObjectMapper mapper;

	private final Map<Network, BitcoinJSONRPCClient> bitcoindRpcClients;

	static {
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private BTCDRPCBitcoinConnection(Map<Network, BitcoinJSONRPCClient> bitcoindRpcClients) {
		this.bitcoindRpcClients = bitcoindRpcClients;
	}

	public static BTCDRPCBitcoinConnection create(Map<Network, URL> rpcUrls) {
		return new BTCDRPCBitcoinConnection(rpcUrls.entrySet()
				.stream()
				.map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), new BitcoinJSONRPCClient(e.getValue())))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	public static BTCDRPCBitcoinConnection create() {
		return create(Map.of(
				Network.mainnet, BitcoinJSONRPCClient.DEFAULT_JSONRPC_URL,
				Network.testnet, BitcoinJSONRPCClient.DEFAULT_JSONRPC_TESTNET_URL,
				Network.regtest, BitcoinJSONRPCClient.DEFAULT_JSONRPC_REGTEST_URL)
		);
	}

	public BitcoinJSONRPCClient getBitcoinRpcClient(Network network) {
		return this.bitcoindRpcClients.get(network);
	}

	@Override
	public Block getBlockByBlockHeight(Network network, Integer blockHeight) {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public Tx getTransactionById(Network network, String txid) {
		throw new RuntimeException("Not implemented yet");
	}
}
