package uniresolver.driver.did.btc1.crud.read.records;

import foundation.identity.did.Service;
import org.bitcoinj.base.Address;

public record Beacon(Address address, String serviceEndpoint, /* TODO: extra, not in spec */ Service beaconService) {
}
