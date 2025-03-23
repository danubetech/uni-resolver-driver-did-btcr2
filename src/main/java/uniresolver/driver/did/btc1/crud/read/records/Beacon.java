package uniresolver.driver.did.btc1.crud.read.records;

import org.bitcoinj.base.Address;

public record Beacon(Address address, String serviceEndpoint) {
}
