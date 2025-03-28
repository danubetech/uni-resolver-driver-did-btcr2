package uniresolver.driver.did.btc1.bitcoinconnection;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.bitcoinj.kits.WalletAppKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.bitcoinconnection.records.Block;
import uniresolver.driver.did.btc1.bitcoinconnection.records.Tx;

import java.io.File;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BitcoinjSPVBitcoinConnection extends AbstractBitcoinConnection implements BitcoinConnection {

	private static final Logger log = LoggerFactory.getLogger(BitcoindRPCBitcoinConnection.class);

	private static final ObjectMapper mapper;

	private final Map<Network, WalletAppKit> walletAppKits;

	static {
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private BitcoinjSPVBitcoinConnection(Map<Network, WalletAppKit> walletAppKits) {
		this.walletAppKits = walletAppKits;
	}

	public static BitcoinjSPVBitcoinConnection create(List<Network> networks) {
		return new BitcoinjSPVBitcoinConnection(networks
				.stream()
				.map(network -> new AbstractMap.SimpleEntry<>(network, WalletAppKit.launch(network.toBitcoinjNetwork(), new File("."), network.name())))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	public static BitcoinjSPVBitcoinConnection create() {
		return create(List.of(
				Network.mainnet,
				Network.testnet,
				Network.regtest)
		);
	}

	public WalletAppKit getWalletAppKit(Network network) {
		return this.walletAppKits.get(network);
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