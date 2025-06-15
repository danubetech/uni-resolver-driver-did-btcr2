package uniresolver.driver.did.btc1;

import fr.acinq.bitcoin.BlockHash;
import fr.acinq.bitcoin.Chain;

public class Test {

    public static void main(String[] args) throws Exception {
        Network network;

        System.out.println(Chain.Mainnet.INSTANCE.getChainHash());
        System.out.println(Chain.Signet.INSTANCE.getChainHash());
        System.out.println(Chain.Regtest.INSTANCE.getChainHash());
        System.out.println(Chain.Testnet3.INSTANCE.getChainHash());
        System.out.println(Chain.Testnet4.INSTANCE.getChainHash());
    }
}
