package uniresolver.driver.did.btc1.crud.read.records;

import java.util.List;

public record NextSignals(Integer blockHeight, List<Signal> signals) {
}
