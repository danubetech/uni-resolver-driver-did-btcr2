package uniresolver.driver.did.btc1.connections.bitcoin.records;

public record TxOut(
        String txId,
        String scriptPubKeyAddress,
        String asm) {
}
