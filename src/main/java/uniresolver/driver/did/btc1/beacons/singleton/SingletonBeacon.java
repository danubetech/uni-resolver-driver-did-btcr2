package uniresolver.driver.did.btc1.beacons.singleton;

import foundation.identity.did.Service;
import org.bitcoinj.base.Address;
import org.bitcoinj.uri.BitcoinURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.crud.read.ResolveTargetDocument;

import java.net.URI;
import java.util.Map;

public class SingletonBeacon {

    private static final Logger log = LoggerFactory.getLogger(SingletonBeacon.class);

    public static final String TYPE = "SingletonBeacon";

    /*
     * 5.1.1 Establish Singleton Beacon
     */

    // See https://dcdpr.github.io/did-btc1/#establish-singleton-beacon
    public static Service establishSingletonBeacon(URI serviceId, Address beaconAddress, Network network) {

        URI bip21ServiceEndpoint = URI.create(BitcoinURI.convertToBitcoinURI(network.toBitcoinjNetwork(), beaconAddress.toString(), null, null, null));

        Service.Builder<? extends Service.Builder<?>> serviceBuilder = Service.builder();
        serviceBuilder.id(serviceId);
        serviceBuilder.type(TYPE);
        serviceBuilder.serviceEndpoint(bip21ServiceEndpoint);

        Service service = serviceBuilder.build();

        if (log.isDebugEnabled()) log.debug("establishSingletonBeacon: " + service);
        return service;
    }

    /*
     * 5.1.3 Process Singleton Beacon Signal
     */

    // See https://dcdpr.github.io/did-btc1/#process-singleton-beacon-signal
    public static ResolveTargetDocument.Update processSingletonBeaconSignal(ResolveTargetDocument.Tx signalTx, Map<String, Object> signalSidecarData) {

        // TODO
        return null;
    }
}
