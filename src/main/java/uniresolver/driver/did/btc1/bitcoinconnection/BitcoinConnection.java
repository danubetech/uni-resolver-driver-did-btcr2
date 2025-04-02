package uniresolver.driver.did.btc1.bitcoinconnection;

import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.bitcoinconnection.records.Block;
import uniresolver.driver.did.btc1.bitcoinconnection.records.Tx;

public interface BitcoinConnection {

    Block getBlockByBlockHeight(Network network, Integer blockHeight);
    Tx getTransactionById(Network network, String txid);
    Block getBlockByTargetTime(Network network, Long targetTime);
    Block getBlockByMinConfirmations(Network network, Integer confirmations);
}
