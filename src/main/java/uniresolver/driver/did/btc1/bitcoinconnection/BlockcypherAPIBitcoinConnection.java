package uniresolver.driver.did.btc1.bitcoinconnection;

import com.google.gson.Gson;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.bitcoinconnection.records.Block;
import uniresolver.driver.did.btc1.bitcoinconnection.records.Tx;

public class BlockcypherAPIBitcoinConnection extends AbstractBitcoinConnection implements BitcoinConnection {

	public BlockcypherAPIBitcoinConnection() {

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
