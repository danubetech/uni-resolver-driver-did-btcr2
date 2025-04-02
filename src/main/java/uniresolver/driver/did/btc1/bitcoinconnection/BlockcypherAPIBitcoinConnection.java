package uniresolver.driver.did.btc1.bitcoinconnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.bitcoinconnection.records.Block;
import uniresolver.driver.did.btc1.bitcoinconnection.records.Tx;

public class BlockcypherAPIBitcoinConnection extends AbstractBitcoinConnection implements BitcoinConnection {

	private static final Logger log = LoggerFactory.getLogger(BlockcypherAPIBitcoinConnection.class);

	private BlockcypherAPIBitcoinConnection() {
		if (log.isDebugEnabled()) log.debug("Creating BlockcypherAPIBitcoinConnection");
	}

	public static BlockcypherAPIBitcoinConnection create() {
		return new BlockcypherAPIBitcoinConnection();
	}

	@Override
	public Block getBlockByBlockHeight(Network network, Integer blockHeight) {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public Tx getTransactionById(Network network, String txid) {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public Block getBlockByTargetTime(Network network, Long targetTime) {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public Block getBlockByMinConfirmations(Network network, Integer confirmations) {
		throw new RuntimeException("Not implemented yet");
	}
}
