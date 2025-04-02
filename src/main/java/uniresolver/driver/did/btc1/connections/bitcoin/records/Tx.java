package uniresolver.driver.did.btc1.connections.bitcoin.records;

import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

import java.util.List;

public record Tx(
        String txId,
        List<TxIn> txIns,
        List<TxOut> txOuts) {

    private static final String COINBASE_TX_IDENTIFIER = "0000000000000000000000000000000000000000000000000000000000000000";
    private static final String GENESIS_TX_IDENTIFIER = "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b";

    public static Tx fromBitcoinjRawTransaction(BitcoinJSONRPCClient bitcoinJSONRPCClient, String txId) {
        if (COINBASE_TX_IDENTIFIER.equals(txId) || GENESIS_TX_IDENTIFIER.equals(txId)) {
            return new Tx(txId, null, null);
        }
        wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction bitcoinjRawTransaction = bitcoinJSONRPCClient.getRawTransaction(txId);
        List<TxIn> txIns = bitcoinjRawTransaction.vIn().stream().map(in -> new TxIn(in.txid())).toList();
        List<TxOut> txOuts = bitcoinjRawTransaction.vOut().stream().map(out -> new TxOut(out.transaction().txId(), out.scriptPubKey().addresses(), out.scriptPubKey().asm())).toList();
        return new Tx(txId, txIns, txOuts);
    }
}
