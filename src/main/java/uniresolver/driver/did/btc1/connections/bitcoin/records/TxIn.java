package uniresolver.driver.did.btc1.connections.bitcoin.records;

public record TxIn(
        String txId,
        Integer transactionOutputN) {

    public static TxIn fromBitcoinjIn(wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction.In in) {
        String txId = in.txid();
        Integer transactionOutputN = txId == null ? null : in.getTransactionOutput().n();
        return new TxIn(txId, transactionOutputN);
    }
}
