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
import uniresolver.driver.did.btc1.crud.update.jsonld.DIDUpdatePayload;
import uniresolver.driver.did.btc1.util.HexUtil;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
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

    //private static final Pattern PATTERN_TXOUT = Pattern.compile("^OP_RETURN OP_PUSH32 ([0-9a-fA-F]{32})$");
    private static final Pattern PATTERN_TXOUT = Pattern.compile("^OP_RETURN ([0-9a-fA-F]{64})$");

    // See https://dcdpr.github.io/did-btc1/#process-singleton-beacon-signal
    public static DIDUpdatePayload processSingletonBeaconSignal(Tx tx, Map<String, Object> signalSidecarData, IPFSConnection ipfsConnection, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) throws ResolutionException {

        TxOut txOut = tx.txOuts().getFirst();

        DIDUpdatePayload didUpdatePayload = null;

        Matcher matcher = PATTERN_TXOUT.matcher(txOut.asm());
        if (! matcher.matches()) {
            if (log.isInfoEnabled()) log.debug("processSingletonBeaconSignal: Not a beacon signal: " + didUpdatePayload);
            return didUpdatePayload;
        }

        byte[] hashBytes = HexUtil.hexDecode(matcher.group(1));

        if (signalSidecarData != null) {
            Map<String, Object> didUpdatePayloadMap = (Map<String, Object>) signalSidecarData.get("updatePayload");
            byte[] updateHashBytes = JsonCanonicalizationAndHash.jsonCanonicalizationAndHash(didUpdatePayloadMap);
            if (! Arrays.equals(updateHashBytes, hashBytes)) {
                throw new ResolutionException("invalidSidecarData", "updateHashBytes " + HexUtil.hexEncode(updateHashBytes) + " does not match hashBytes: " + HexUtil.hexEncode(hashBytes));
            }
            didUpdatePayload = DIDUpdatePayload.fromMap(didUpdatePayloadMap);
        } else {
            didUpdatePayload = FetchContentFromAddressableStorage.fetchJsonLDObjectContentFromAddressableStorage(hashBytes, DIDUpdatePayload.class, ipfsConnection);
            if (didUpdatePayload == null) {
                throw new ResolutionException("latePublishingError", "didUpdatePayload is null");
            }
        }

        // DID DOCUMENT METADATA

        Map<String, Map<String, Object>> didDocumentMetadataDidUpdatePayloads = (Map<String, Map<String, Object>>) didDocumentMetadata.computeIfAbsent("didUpdatePayloads", x -> new LinkedHashMap<>());
        didDocumentMetadataDidUpdatePayloads.put(tx.txId(), didUpdatePayload.toMap());

        // done

        if (log.isDebugEnabled()) log.debug("processSingletonBeaconSignal: " + didUpdatePayload);
        return didUpdatePayload;
    }
}
