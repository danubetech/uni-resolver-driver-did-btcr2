package uniresolver.driver.did.btc1;

import org.bitcoinj.base.BitcoinNetwork;

import java.util.Arrays;

public enum Network {

    bitcoin,
    signet,
    regtest,
    testnet3,
    testnet4;

    public static String[] stringValues() {
        return Arrays.stream(Network.values()).map(Network::name).toArray(String[]::new);
    }

    public org.bitcoinj.base.BitcoinNetwork toBitcoinjNetwork() {
        return switch (this) {
            case bitcoin -> BitcoinNetwork.MAINNET;
            case signet -> BitcoinNetwork.SIGNET;
            case regtest -> BitcoinNetwork.REGTEST;
            case testnet3 -> BitcoinNetwork.TESTNET;
            case testnet4 -> BitcoinNetwork.TESTNET;   /* TODO: what is the difference between v3 and v4 ? */
        };
    }
}
