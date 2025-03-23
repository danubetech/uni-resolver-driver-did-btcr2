package uniresolver.driver.did.btc1.bitcoinconnection;

import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.bitcoinconnection.records.Block;
import uniresolver.driver.did.btc1.util.URLUtil;

import java.util.Map;

public interface BitcoinConnection {

    BitcoinConnection bitcoinConnection = BitcoindRPCBitcoinConnection.create(Map.of(
            Network.mainnet, URLUtil.url("http://localhost:18443"),
            Network.testnet, URLUtil.url("http://localhost:18443"),
            Network.regtest, URLUtil.url("http://localhost:18443")
    ));

    static BitcoinConnection getInstance() {
        return bitcoinConnection;
    }

    Block getBlockByBlockHeight(Network network, Integer blockHeight);
}
