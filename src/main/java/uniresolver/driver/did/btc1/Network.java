package uniresolver.driver.did.btc1;

import org.bitcoinj.base.BitcoinNetwork;

public enum Network {

    bitcoin,
    signet,
    regtest,
    testnet3,
    testnet4;

    public static Network valueOf(Byte byteValue) {
        return switch(byteValue) {
            case 0 -> Network.bitcoin;
            case 1 -> Network.signet;
            case 2 -> Network.regtest;
            case 3 -> Network.testnet3;
            case 4 -> Network.testnet4;
            default -> throw new IllegalArgumentException("Unsupported 'network' value: " + byteValue);
        };
    }

    public int toInt() {
        return switch (this) {
            case bitcoin -> 0x00;
            case signet -> 0x01;
            case regtest -> 0x02;
            case testnet3 -> 0x03;
            case testnet4 -> 0x04;
        };
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
