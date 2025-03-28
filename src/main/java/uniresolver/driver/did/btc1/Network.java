package uniresolver.driver.did.btc1;

import org.bitcoinj.base.BitcoinNetwork;

import java.util.Arrays;

public enum Network {

    mainnet,
    signet,
    testnet,
    regtest;

    public static String[] stringValues() {
        return Arrays.stream(Network.values()).map(Network::name).toArray(String[]::new);
    }

    public org.bitcoinj.base.BitcoinNetwork toBitcoinjNetwork() {
        return switch (this) {
            case mainnet -> BitcoinNetwork.MAINNET;
            case signet -> BitcoinNetwork.SIGNET;
            case testnet -> BitcoinNetwork.TESTNET;
            case regtest -> BitcoinNetwork.REGTEST;
        };
    }
}
