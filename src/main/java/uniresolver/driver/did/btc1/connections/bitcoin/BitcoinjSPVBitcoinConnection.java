package uniresolver.driver.did.btc1.connections.bitcoin;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.bitcoinj.kits.WalletAppKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.connections.bitcoin.records.Block;
import uniresolver.driver.did.btc1.connections.bitcoin.records.Tx;

import java.io.File;

public class BitcoinjSPVBitcoinConnection extends AbstractBitcoinConnection implements BitcoinConnection {

	private static final Logger log = LoggerFactory.getLogger(BitcoindRPCBitcoinConnection.class);

	private static final ObjectMapper mapper;

	private final WalletAppKit walletAppKit;

	static {
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private BitcoinjSPVBitcoinConnection(WalletAppKit walletAppKit) {
		if (log.isDebugEnabled()) log.debug("Creating BitcoindRPCBitcoinConnection: " + walletAppKit);
		this.walletAppKit = walletAppKit;
	}

	public static BitcoinjSPVBitcoinConnection create(Network network) {
		if (log.isDebugEnabled()) log.debug("Creating BitcoindRPCBitcoinConnection: " + network);
		return new BitcoinjSPVBitcoinConnection(WalletAppKit.launch(network.toBitcoinjNetwork(), new File("."), network.name()));
	}

	@Override
	public Block getBlockByBlockHeight(Integer blockHeight) {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public Tx getTransactionById(String txid) {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public Block getBlockByTargetTime(Long targetTime) {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public Block getBlockByMinConfirmations(Integer confirmations) {
		throw new RuntimeException("Not implemented yet");
	}

	/*
	 * Getters and setters
	 */

	public WalletAppKit getWalletAppKit() {
		return walletAppKit;
	}
}