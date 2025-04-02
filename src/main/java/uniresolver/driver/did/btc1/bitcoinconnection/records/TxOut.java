package uniresolver.driver.did.btc1.bitcoinconnection.records;

import java.util.List;

public record TxOut(
        String txId,
        List<String> scriptPubKeyAddresses,
        String asm) {
}
