package uniresolver.driver.did.btc1.connections.bitcoin;

import org.bitcoinj.base.Address;
import uniresolver.driver.did.btc1.connections.bitcoin.records.Block;
import uniresolver.driver.did.btc1.connections.bitcoin.records.Tx;

import java.util.List;

public abstract class AbstractBitcoinConnection implements BitcoinConnection {

    @Override
    public Block getBlockByBlockHeight(Integer blockHeight) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Tx getTransactionById(String txid) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Block getBlockByTargetTime(Long targetTime) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Block getBlockByMinConfirmations(Integer confirmations) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<Tx> getAddressTransactions(Address address) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Block getBlockByTransaction(String txid) {
        throw new RuntimeException("Not implemented");
    }
}
