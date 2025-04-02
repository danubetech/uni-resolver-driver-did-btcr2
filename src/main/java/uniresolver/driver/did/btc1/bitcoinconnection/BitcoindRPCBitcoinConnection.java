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
				Network.mainnet, BitcoinJSONRPCClient.DEFAULT_JSONRPC_URL,
				Network.testnet, BitcoinJSONRPCClient.DEFAULT_JSONRPC_TESTNET_URL,
				Network.regtest, BitcoinJSONRPCClient.DEFAULT_JSONRPC_REGTEST_URL)
		);
	}

	public BitcoinJSONRPCClient getBitcoinRpcClient(Network network) {
		BitcoinJSONRPCClient bitcoinJSONRPCClient = this.bitcoindRpcClients.get(network);
		if (log.isDebugEnabled()) log.debug("getBitcoinRpcClient for " + network + ": " + bitcoinJSONRPCClient.rpcURL);
		return bitcoinJSONRPCClient;
	}

	@Override
	public Block getBlockByBlockHeight(Network network, Integer blockHeight) {
		BitcoinJSONRPCClient bitcoinJSONRPCClient = this.getBitcoinRpcClient(network);
		wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Block bitcoinjBlock = bitcoinJSONRPCClient.getBlock(blockHeight);
		List<BitcoindRpcClient.Transaction> bitcoinjTransactions = bitcoinjBlock.tx().stream().map(tx -> bitcoinJSONRPCClient.getTransaction(tx)).toList();
		List<Tx> txs = bitcoinjTransactions.stream().map(bitcoinjTransaction -> {
			BitcoindRpcClient.RawTransaction bitcoinjRawTransaction = bitcoinjTransaction.raw();
			List<TxIn> txIns = bitcoinjRawTransaction.vIn().stream().map(in -> new TxIn(in.getTransaction().txId())).toList();
			List<TxOut> txOuts = bitcoinjRawTransaction.vOut().stream().map(out -> new TxOut(out.transaction().txId(), out.scriptPubKey().addresses(), out.scriptPubKey().asm())).toList();
			return new Tx(bitcoinjTransaction.txId(), txIns, txOuts);
		}).toList();
		return new Block(bitcoinjBlock.height(), bitcoinjBlock.hash(), txs);
	}

	@Override
	public Tx getTransactionById(Network network, String txid) {
		BitcoinJSONRPCClient bitcoinJSONRPCClient = this.getBitcoinRpcClient(network);
		wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Transaction bitcoinjTransaction = bitcoinJSONRPCClient.getTransaction(txid);
		BitcoindRpcClient.RawTransaction bitcoinjRawTransaction = bitcoinjTransaction.raw();
		List<TxIn> txIns = bitcoinjRawTransaction.vIn().stream().map(in -> new TxIn(in.getTransaction().txId())).toList();
		List<TxOut> txOuts = bitcoinjRawTransaction.vOut().stream().map(out -> new TxOut(out.transaction().txId(), out.scriptPubKey().addresses(), out.scriptPubKey().asm())).toList();
		return new Tx(bitcoinjTransaction.txId(), txIns, txOuts);
	}

	@Override
	public Block getBlockByTargetTime(Network network, Long targetTime) {
		BitcoinJSONRPCClient bitcoinJSONRPCClient = this.getBitcoinRpcClient(network);
		Integer blocks = bitcoinJSONRPCClient.getBlockChainInfo().blocks();
		for (int blockHeight=blocks-1; blockHeight>=0; blockHeight--) {
			wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Block bitcoinjBlock = bitcoinJSONRPCClient.getBlock(blockHeight);
			if (bitcoinjBlock.time().getTime() < targetTime) {
				return new Block(bitcoinjBlock.height(), bitcoinjBlock.hash(), null);
			}
		}
		return null;
	}

	@Override
	public Block getBlockByMinConfirmations(Network network, Integer minConfirmations) {
		BitcoinJSONRPCClient bitcoinJSONRPCClient = this.getBitcoinRpcClient(network);
		Integer blocks = bitcoinJSONRPCClient.getBlockChainInfo().blocks();
		for (int blockHeight=blocks-1; blockHeight>=0; blockHeight--) {
			wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Block bitcoinjBlock = bitcoinJSONRPCClient.getBlock(blockHeight);
			if (bitcoinjBlock.confirmations() >= minConfirmations) {
				return new Block(bitcoinjBlock.height(), bitcoinjBlock.hash(), null);
			}
		}
		return null;
	}
}
