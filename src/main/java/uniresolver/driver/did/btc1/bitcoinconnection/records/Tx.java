package uniresolver.driver.did.btc1.bitcoinconnection.records;

import java.util.List;

public record Tx(String id, List<TxIn> txIns, List<TxOut> txOuts) {
}
