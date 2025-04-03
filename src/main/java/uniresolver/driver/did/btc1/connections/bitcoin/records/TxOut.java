package uniresolver.driver.did.btc1.connections.bitcoin.records;

public record TxOut(
        String txId,
        String scriptPubKeyAddress,
        String asm) {

    public static TxOut fromBitcoinjOut(wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction.Out out) {
        String txId = out.transaction().txId();
        String scriptPubKeyAddress = out.scriptPubKey().mapStr("address");
        String asm = out.scriptPubKey().asm();
        return new TxOut(txId, scriptPubKeyAddress, asm);
    }
}
