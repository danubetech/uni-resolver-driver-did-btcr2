package uniresolver.driver.did.btcr2.crud.read.records;

import org.bitcoinj.base.Address;

public record Beacon(
        String id,
        String type,
        String serviceEndpoint,
        Address address) {
}
