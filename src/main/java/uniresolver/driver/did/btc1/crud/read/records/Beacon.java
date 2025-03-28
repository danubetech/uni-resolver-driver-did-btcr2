package uniresolver.driver.did.btc1.crud.read.records;

import org.bitcoinj.base.Address;

import java.net.URI;

public record Beacon(
        URI id,
        String type,
        String serviceEndpoint,
        Address address) {
}
