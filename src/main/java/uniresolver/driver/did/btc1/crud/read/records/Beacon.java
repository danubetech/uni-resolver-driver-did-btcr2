package uniresolver.driver.did.btc1.crud.read.records;

import org.bitcoinj.base.Address;

public record Beacon(
        String id,
        String type,
        String serviceEndpoint,
        Address address) {
}
