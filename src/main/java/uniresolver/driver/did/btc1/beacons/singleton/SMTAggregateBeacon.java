package uniresolver.driver.did.btc1.beacons.singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btc1.bitcoinconnection.records.Tx;
import uniresolver.driver.did.btc1.crud.read.records.Update;

import java.util.Map;

public class SMTAggregateBeacon {

    private static final Logger log = LoggerFactory.getLogger(SMTAggregateBeacon.class);

    public static final String TYPE = "SMTAggregateBeacon";

    /*
     * 5.3.3 Process SMTAggregate Beacon Signal
     */

    // See https://dcdpr.github.io/did-btc1/#process-smtaggregate-beacon-signal
    public static Update processSMTAggregateBeaconSignal(Tx signalTx, Map<String, Object> signalSidecarData) {

        // TODO
        return null;
    }
}
