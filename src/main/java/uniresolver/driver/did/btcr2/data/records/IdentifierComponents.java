package uniresolver.driver.did.btcr2.data.records;

import uniresolver.driver.did.btcr2.Network;

public record IdentifierComponents(
        int version,
        Network network,
        byte[] genesisBytes,
        GenesisBytesType genesisBytesType) {
}
