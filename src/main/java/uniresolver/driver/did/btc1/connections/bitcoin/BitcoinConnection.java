package uniresolver.driver.did.btc1.connections.bitcoin;

import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.connections.bitcoin.records.Block;
import uniresolver.driver.did.btc1.connections.bitcoin.records.Tx;

public interface BitcoinConnection {

    Block getBlockByBlockHeight(Integer blockHeight);
    Tx getTransactionById(String txid);
    Block getBlockByTargetTime(Long targetTime);
    Block getBlockByMinConfirmations(Integer confirmations);
}
