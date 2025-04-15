package uniresolver.driver.did.btc1;

import org.bitcoinj.base.BitcoinNetwork;

public enum Network {

    bitcoin,
    signet,
    regtest,
    testnet3,
    testnet4,
    user1,
    user2,
    user3,
    user4,
    user5,
    user6,
    user7,
    user8;

    public static Network valueOf(Object value) {
        if (value instanceof String) return Network.valueOf((String) value);
        if (value instanceof Number) return Network.valueOf(((Number) value).intValue());
        throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
    }

    public static Network valueOf(Byte byteValue) {
        return switch(byteValue) {
            case 0x00 -> Network.bitcoin;
            case 0x01 -> Network.signet;
            case 0x02 -> Network.regtest;
            case 0x03 -> Network.testnet3;
            case 0x04 -> Network.testnet4;
            case 0x05 -> throw new IllegalArgumentException("Unsupported 'network' value: " + byteValue);
            case 0x06 -> throw new IllegalArgumentException("Unsupported 'network' value: " + byteValue);
            case 0x07 -> throw new IllegalArgumentException("Unsupported 'network' value: " + byteValue);
            case 0x08 -> valueOf(byteValue.intValue() - 7);
            case 0x09 -> valueOf(byteValue.intValue() - 7);
            case 0x0A -> valueOf(byteValue.intValue() - 7);
            case 0x0B -> valueOf(byteValue.intValue() - 7);
            case 0x0C -> valueOf(byteValue.intValue() - 7);
            case 0x0D -> valueOf(byteValue.intValue() - 7);
            case 0x0E -> valueOf(byteValue.intValue() - 7);
            case 0x0F -> valueOf(byteValue.intValue() - 7);
            default -> throw new IllegalArgumentException("Unsupported 'network' value: " + byteValue);
        };
    }

    public static Network valueOf(Integer intValue) {
        return switch(intValue) {
            case 0x01 -> Network.user1;
            case 0x02 -> Network.user2;
            case 0x03 -> Network.user3;
            case 0x04 -> Network.user4;
            case 0x05 -> Network.user5;
            case 0x06 -> Network.user6;
            case 0x07 -> Network.user7;
            case 0x08 -> Network.user8;
            default -> throw new IllegalArgumentException("Unsupported 'network' value: " + intValue);
        };
    }

    public int toInt() {
        return switch (this) {
            case bitcoin -> 0x00;
            case signet -> 0x01;
            case regtest -> 0x02;
            case testnet3 -> 0x03;
            case testnet4 -> 0x04;
            case user1 -> 0x08;
            case user2 -> 0x09;
            case user3 -> 0x0a;
            case user4 -> 0x0b;
            case user5 -> 0x0c;
            case user6 -> 0x0d;
            case user7 -> 0x0e;
            case user8 -> 0x0f;
        };
    }

    public org.bitcoinj.base.BitcoinNetwork toBitcoinjNetwork() {
        return switch (this) {
            case bitcoin -> BitcoinNetwork.MAINNET;
            case signet -> BitcoinNetwork.SIGNET;
            case regtest -> BitcoinNetwork.REGTEST;
            case testnet3 -> BitcoinNetwork.TESTNET;
            case testnet4 -> BitcoinNetwork.TESTNET;   /* TODO: what is the difference between v3 and v4 ? */
            case user1 -> throw new IllegalArgumentException("Unknown 'network': " + this);
            case user2 -> throw new IllegalArgumentException("Unknown 'network': " + this);
            case user3 -> throw new IllegalArgumentException("Unknown 'network': " + this);
            case user4 -> throw new IllegalArgumentException("Unknown 'network': " + this);
            case user5 -> throw new IllegalArgumentException("Unknown 'network': " + this);
            case user6 -> throw new IllegalArgumentException("Unknown 'network': " + this);
            case user7 -> throw new IllegalArgumentException("Unknown 'network': " + this);
            case user8 -> throw new IllegalArgumentException("Unknown 'network': " + this);
        };
    }
}
