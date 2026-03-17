package uniresolver.driver.did.btcr2.beacons.singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btcr2.connections.bitcoin.records.Tx;
import uniresolver.driver.did.btcr2.data.jsonld.BTCR2Update;

import java.util.Map;

public class SMTBeacon {

    private static final Logger log = LoggerFactory.getLogger(SMTBeacon.class);

    public static final String TYPE = "SMTBeacon";

    /*
     * 5.3.3 Process SMTAggregate Beacon Signal
     */

    // See https://dcdpr.github.io/did-btcr2/#process-smtaggregate-beacon-signal
    public static BTCR2Update processSMTAggregateBeaconSignal(Tx signalTx, Map<String, Object> signalSidecarData) {

        // TODO
        return null;
    }
}
