package uniresolver.driver.did.btc1.beacons.singleton;

import foundation.identity.did.Service;
import org.bitcoinj.base.Address;
import org.bitcoinj.uri.BitcoinURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.appendix.FetchContentFromAddressableStorage;
import uniresolver.driver.did.btc1.appendix.JsonCanonicalizationAndHash;
import uniresolver.driver.did.btc1.connections.bitcoin.records.Tx;
import uniresolver.driver.did.btc1.connections.bitcoin.records.TxOut;
import uniresolver.driver.did.btc1.connections.ipfs.IPFSConnection;
import uniresolver.driver.did.btc1.crud.update.jsonld.DIDUpdate;
import uniresolver.driver.did.btc1.util.HexUtil;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern PATTERN_TXOUT = Pattern.compile("^OP_RETURN OP_PUSHBYTES_32 ([0-9a-fA-F]{64})$");

    // See https://dcdpr.github.io/did-btc1/#process-singleton-beacon-signal
    public static DIDUpdate processSingletonBeaconSignal(Tx tx, Map<String, Object> signalSidecarData, IPFSConnection ipfsConnection, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) throws ResolutionException {

        // Initialize a txOut variable to the 0th transaction output of the tx.
        // TODO: This is incorrect, should be last instead of 0th?

        TxOut txOut = tx.txOuts().getLast();

        // Set didUpdatePayload to null.

        DIDUpdate didUpdate = null;

        // Set hashBytes to the 32 bytes in the txOut.

        Matcher matcher = PATTERN_TXOUT.matcher(txOut.asm());
        if (! matcher.matches()) {
            if (log.isInfoEnabled()) log.debug("processSingletonBeaconSignal: Not a beacon signal: " + didUpdate);
            return didUpdate;
        }
        byte[] hashBytes = HexUtil.hexDecode(matcher.group(1));

        // If signalSidecarData:

        if (signalSidecarData != null) {

            // Set didUpdatePayload to signalSidecarData.updatePayload

            Map<String, Object> didUpdatePayloadMap = (Map<String, Object>) signalSidecarData.get("updatePayload");

            // Set updateHashBytes to the result of passing didUpdatePayload to the JSON Canonicalization and Hash algorithm.

            byte[] updateHashBytes = JsonCanonicalizationAndHash.jsonCanonicalizationAndHash(didUpdatePayloadMap);

            // If updateHashBytes does not equal hashBytes, MUST throw an invalidSidecarData error.

            if (! Arrays.equals(updateHashBytes, hashBytes)) {
                throw new ResolutionException("invalidSidecarData", "updateHashBytes " + HexUtil.hexEncode(updateHashBytes) + " does not match hashBytes: " + HexUtil.hexEncode(hashBytes));
            }

            // Return didUpdatePayload

            didUpdate = DIDUpdate.fromMap(didUpdatePayloadMap);

        // Else:

        } else {

            // Set didUpdatePayload to the result of passing hashBytes into the Fetch Content from Addressable Storage algorithm.

            didUpdate = FetchContentFromAddressableStorage.fetchJsonLDObjectContentFromAddressableStorage(hashBytes, DIDUpdate.class, ipfsConnection);

            // If didUpdatePayload is null, MUST raise a latePublishingError. MAY identify Beacon Signal to resolver and request
            // additional Sidecar data be provided.

            if (didUpdate == null) {
                throw new ResolutionException("latePublishingError", "didUpdatePayload is null for beacon signal");
            }
        }

        // Return didUpdatePayload.

        List<Map<String, Object>> didDocumentMetadataSignals = (List<Map<String, Object>>) didDocumentMetadata.get("signals");
        log.warn(didDocumentMetadataSignals.toString());
        Map<String, Object> didDocumentMetadataSignal = didDocumentMetadataSignals.stream().filter(x -> x.get("signalId").equals(tx.txId())).findFirst().get();
        didDocumentMetadataSignal.put("updatePayload", didUpdate.toMap());

        if (log.isDebugEnabled()) log.debug("processSingletonBeaconSignal: " + didUpdate);
        return didUpdate;
    }
}
