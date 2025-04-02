package uniresolver.driver.did.btc1;

import org.bitcoinj.base.BitcoinNetwork;

import java.util.Arrays;

public enum Network {

    mainnet,
    testnet,
    signet,
    regtest;

    public static String[] stringValues() {
        return Arrays.stream(Network.values()).map(Network::name).toArray(String[]::new);
    }

    public org.bitcoinj.base.BitcoinNetwork toBitcoinjNetwork() {
        return switch (this) {
            case mainnet -> BitcoinNetwork.MAINNET;
            case testnet -> BitcoinNetwork.TESTNET;
            case signet -> BitcoinNetwork.SIGNET;
            case regtest -> BitcoinNetwork.REGTEST;
        };
    }
}
