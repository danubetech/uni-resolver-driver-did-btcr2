package uniresolver.driver.did.btc1.connections.bitcoin;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.connections.bitcoin.records.Block;
import uniresolver.driver.did.btc1.connections.bitcoin.records.Tx;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

import java.net.URL;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BitcoindRPCBitcoinConnection extends AbstractBitcoinConnection implements BitcoinConnection {

	private static final Logger log = LoggerFactory.getLogger(BitcoindRPCBitcoinConnection.class);

	private static final ObjectMapper mapper;

	private final Map<Network, BitcoinJSONRPCClient> bitcoindRpcClients;

	static {
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private BitcoindRPCBitcoinConnection(Map<Network, BitcoinJSONRPCClient> bitcoindRpcClients) {
		if (log.isDebugEnabled()) log.debug("Creating BitcoindRPCBitcoinConnection: " + bitcoindRpcClients);
		this.bitcoindRpcClients = bitcoindRpcClients;
	}

	public static BitcoindRPCBitcoinConnection create(Map<Network, URL> rpcUrls) {
		if (log.isDebugEnabled()) log.debug("Creating BitcoindRPCBitcoinConnection: " + rpcUrls);
		return new BitcoindRPCBitcoinConnection(rpcUrls.entrySet()
				.stream()
				.map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), new BitcoinJSONRPCClient(e.getValue())))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	public static BitcoindRPCBitcoinConnection create() {
		if (log.isDebugEnabled()) log.debug("Creating BitcoindRPCBitcoinConnection");
		return create(Map.of(
				Network.bitcoin, BitcoinJSONRPCClient.DEFAULT_JSONRPC_URL,
				Network.regtest, BitcoinJSONRPCClient.DEFAULT_JSONRPC_REGTEST_URL,
				Network.testnet3, BitcoinJSONRPCClient.DEFAULT_JSONRPC_TESTNET_URL,
				Network.testnet4, BitcoinJSONRPCClient.DEFAULT_JSONRPC_TESTNET_URL)
		);
	}

	public BitcoinJSONRPCClient getBitcoinJsonRpcClient(Network network) {
		BitcoinJSONRPCClient bitcoinJSONRPCClient = this.bitcoindRpcClients.get(network);
		if (log.isDebugEnabled()) log.debug("getBitcoinJsonRpcClient for " + network + ": " + bitcoinJSONRPCClient.rpcURL);
		return bitcoinJSONRPCClient;
	}

	@Override
	public Block getBlockByBlockHeight(Network network, Integer blockHeight) {
		BitcoinJSONRPCClient bitcoinJSONRPCClient = this.getBitcoinJsonRpcClient(network);
		wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Block bitcoinjBlock = bitcoinJSONRPCClient.getBlock(blockHeight);
		List<Tx> txs = bitcoinjBlock.tx().stream().map(tx -> Tx.fromBitcoinjRawTransaction(bitcoinJSONRPCClient, tx)).collect(Collectors.toList());
		Block block = new Block(bitcoinjBlock.height(), bitcoinjBlock.hash(), txs);
		if (log.isDebugEnabled()) log.debug("getBlockByBlockHeight for {} and {}: {}", network, blockHeight, block);
		return block;
	}

	@Override
	public Tx getTransactionById(Network network, String txId) {
		BitcoinJSONRPCClient bitcoinJSONRPCClient = this.getBitcoinJsonRpcClient(network);
		Tx tx = Tx.fromBitcoinjRawTransaction(bitcoinJSONRPCClient, txId);
		if (log.isDebugEnabled()) log.debug("getTransactionById for {} and {}: {}", network, txId, tx);
		return tx;
	}

	@Override
	public Block getBlockByTargetTime(Network network, Long targetTime) {
		BitcoinJSONRPCClient bitcoinJSONRPCClient = this.getBitcoinJsonRpcClient(network);
		Integer blocks = bitcoinJSONRPCClient.getBlockChainInfo().blocks();
		Block block = null;
		for (int blockHeight=blocks-1; blockHeight>=0; blockHeight--) {
			wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Block bitcoinjBlock = bitcoinJSONRPCClient.getBlock(blockHeight);
			if (bitcoinjBlock.time().getTime() < targetTime) {
				block = new Block(bitcoinjBlock.height(), bitcoinjBlock.hash(), null);
				break;
			}
		}
		if (log.isDebugEnabled()) log.debug("getBlockByTargetTime for {} and {}: {}", network, targetTime, block);
		return block;
	}

	@Override
	public Block getBlockByMinConfirmations(Network network, Integer minConfirmations) {
		BitcoinJSONRPCClient bitcoinJSONRPCClient = this.getBitcoinJsonRpcClient(network);
		Integer blocks = bitcoinJSONRPCClient.getBlockChainInfo().blocks();
		Block block = null;
		for (int blockHeight=blocks-1; blockHeight>=0; blockHeight--) {
			wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Block bitcoinjBlock = bitcoinJSONRPCClient.getBlock(blockHeight);
			if (bitcoinjBlock.confirmations() >= minConfirmations) {
				block = new Block(bitcoinjBlock.height(), bitcoinjBlock.hash(), null);
				break;
			}
		}
		if (log.isDebugEnabled()) log.debug("getBlockByMinConfirmations for {} and {}: {}", network, minConfirmations, block);
		return block;
	}
}
