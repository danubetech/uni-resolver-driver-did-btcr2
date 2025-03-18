package uniresolver.driver.did.btc1;

import org.bitcoinj.base.BitcoinNetwork;

public enum Network {

    mainnet,
    signet,
    testnet,
    regnet;

    public org.bitcoinj.base.Network toBitcoinjNetwork() {
        return switch (this) {
            case mainnet -> BitcoinNetwork.MAINNET;
            case signet -> BitcoinNetwork.SIGNET;
            case testnet -> BitcoinNetwork.TESTNET;
            case regnet -> BitcoinNetwork.REGTEST;
        };
    }
}
