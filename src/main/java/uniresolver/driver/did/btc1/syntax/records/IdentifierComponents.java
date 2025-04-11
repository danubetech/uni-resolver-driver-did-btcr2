package uniresolver.driver.did.btc1.syntax.records;

import uniresolver.driver.did.btc1.Network;

public record IdentifierComponents(
        String idType,
        Integer version,
        Network network,
        byte[] genesisBytes) {
}
