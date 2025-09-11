package uniresolver.driver.did.btcr2.syntax.records;

import uniresolver.driver.did.btcr2.Network;

public record IdentifierComponents(
        String idType,
        Integer version,
        Network network,
        byte[] genesisBytes) {
}
