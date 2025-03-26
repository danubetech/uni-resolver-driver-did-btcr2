package uniresolver.driver.did.btc1.bitcoinconnection;

import com.google.gson.Gson;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.bitcoinconnection.records.Block;

public class BlockcypherAPIBitcoinConnection extends AbstractBitcoinConnection implements BitcoinConnection {

	public BlockcypherAPIBitcoinConnection() {

	}

	@Override
	public Block getBlockByBlockHeight(Network network, Integer blockHeight) {
		throw new RuntimeException("Not implemented yet");
	}
}
