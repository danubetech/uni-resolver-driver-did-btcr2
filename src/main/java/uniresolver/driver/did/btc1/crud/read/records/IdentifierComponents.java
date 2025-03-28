package uniresolver.driver.did.btc1.crud.read.records;

import uniresolver.driver.did.btc1.Network;

public record IdentifierComponents(
        Network network,
        Integer version,
        String hrp,
        byte[] genesisBytes) {
}
