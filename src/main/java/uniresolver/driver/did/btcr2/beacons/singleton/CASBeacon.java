package uniresolver.driver.did.btcr2.beacons.singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btcr2.connections.bitcoin.records.Tx;
import uniresolver.driver.did.btcr2.data.jsonld.BTCR2Update;

import java.util.Map;

public class CASBeacon {

    private static final Logger log = LoggerFactory.getLogger(CASBeacon.class);

    public static final String TYPE = "CASBeacon";

    /*
     * 5.2.3 Process CIDAggregate Beacon Signal
     */

    // See https://dcdpr.github.io/did-btcr2/#process-cidaggregate-beacon-signal
    public static BTCR2Update processCIDAggregateBeaconSignal(Tx signalTx, Map<String, Object> signalSidecarData) {

        // TODO
        return null;
    }
}
