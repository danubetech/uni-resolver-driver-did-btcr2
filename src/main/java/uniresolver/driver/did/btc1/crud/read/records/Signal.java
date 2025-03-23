package uniresolver.driver.did.btc1.crud.read.records;

import uniresolver.driver.did.btc1.bitcoinconnection.records.Tx;

public record Signal(String beaconId, String beaconType, Tx tx) {
}
