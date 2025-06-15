package uniresolver.driver.did.btc1.crud.read.records;

import uniresolver.driver.did.btc1.connections.bitcoin.records.Tx;

public record BeaconSignal(
        String beaconId,
        String beaconType,
        Tx tx,
        Integer blockheight,
        Long blocktime) {
}
