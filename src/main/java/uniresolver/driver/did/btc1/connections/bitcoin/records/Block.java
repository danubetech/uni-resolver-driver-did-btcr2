package uniresolver.driver.did.btc1.connections.bitcoin.records;

import java.util.List;

public record Block(
        Integer blockHeight,
        String blockHash,
        List<Tx> txs) {
}
