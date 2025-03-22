package uniresolver.driver.did.btc1.crud.read;

import com.google.api.client.util.DateTime;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.Service;
import io.ipfs.api.IPFS;
import org.bitcoinj.base.Address;
import org.bitcoinj.uri.BitcoinURI;
import org.bitcoinj.uri.BitcoinURIParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btc1.beacons.singleton.CIDAggregateBeacon;
import uniresolver.driver.did.btc1.beacons.singleton.SMTAggregateBeacon;
import uniresolver.driver.did.btc1.beacons.singleton.SingletonBeacon;
import uniresolver.driver.did.btc1.util.SHA256Util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ResolveTargetDocument {

    private static final Logger log = LoggerFactory.getLogger(ResolveTargetDocument.class);

    private IPFS ipfs;

    public ResolveTargetDocument(IPFS ipfs) {
        this.ipfs = ipfs;
    }

    /*
     * 4.2.3 Resolve Target Document
     */

    // See https://dcdpr.github.io/did-btc1/#resolve-target-document
    public DIDDocument resolveTargetDocument(DIDDocument initialDocument, Map<String, Object> resolutionOptions) throws ResolutionException {

        Integer targetVersionId = null;
        Long targetTime = null;

        if (resolutionOptions.get("versionId") != null) {
            targetVersionId = (Integer) resolutionOptions.get("versionId");
        } else if (resolutionOptions.get("versionTime") != null) {
            targetTime = DateTime.parseRfc3339((String) resolutionOptions.get("versionTime")).getValue();
        }

        Integer targetBlockheight = determineTargetBlockHeight(targetTime);

        Map<String, Object> sidecarData = (Map<String, Object>) resolutionOptions.get("sidecarData");

        Integer currentVersionId = 1;
        if (currentVersionId.equals(targetVersionId)) return initialDocument;

        Object[] updateHashHistory = new Object[0];

        Integer contemporaryBlockheight = 0;

        DIDDocument contemporaryDIDDocument = initialDocument;

        DIDDocument targetDocument = traverseBlockchainHistory(
                contemporaryDIDDocument,
                contemporaryBlockheight,
                currentVersionId,
                targetVersionId,
                targetBlockheight,
                updateHashHistory,
                sidecarData);

        if (log.isDebugEnabled()) log.debug("resolveTargetDocument: " + targetDocument);
        return targetDocument;
    }

    // See https://dcdpr.github.io/did-btc1/#determine-target-blockheight
    private static Integer determineTargetBlockHeight(Long targetTime) throws ResolutionException {

        Object block;

        if (targetTime != null) {
            // TODO
            block = null;
        } else {
            // TODO
            block = null;
        }

        // TODO
        Integer blockHeight = null;

        if (log.isDebugEnabled()) log.debug("determineTargetBlockHeight: " + blockHeight);
        return blockHeight;
    }

    // See https://dcdpr.github.io/did-btc1/#traverse-blockchain-history
    private static DIDDocument traverseBlockchainHistory(DIDDocument  contemporaryDIDDocument, Integer contemporaryBlockheight, Integer currentVersionId, Integer targetVersionId, Integer targetBlockheight, Object[] updateHashHistory, Map<String, Object> sidecarData) throws ResolutionException {

        // TODO: NEED TO DEAL WITH CANONICALIZATION
        byte[] contemporaryHash = SHA256Util.sha256(contemporaryDIDDocument.toJson().getBytes(StandardCharsets.UTF_8));

        List<Beacon> beacons = new ArrayList<>();
        for (Service service : contemporaryDIDDocument.getServices().stream().filter(service -> Arrays.asList(SingletonBeacon.TYPE, CIDAggregateBeacon.TYPE, SMTAggregateBeacon.TYPE).contains(service.getType())).toList()) {
            Address beaconAddress;
            try {
                beaconAddress = BitcoinURI.of((String) service.getServiceEndpoint()).getAddress();
            } catch (BitcoinURIParseException ex) {
                throw new ResolutionException("invalidDidDocument", "Invalid DID document: " + ex.getMessage(), ex);
            }
            String beaconServiceEndpoint = (String) service.getServiceEndpoint();
            beacons.add(new Beacon(beaconAddress, beaconServiceEndpoint));
        }

        NextSignals nextSignals = findNextSignals(contemporaryBlockheight, beacons);

        List<Signal> signals = nextSignals.signals;

        List<Update> updates = processBeaconSignals(signals, sidecarData);

        // TODO

        DIDDocument targetDocument = null;

        if (log.isDebugEnabled()) log.debug("traverseBlockchainHistory: " + targetDocument);
        return targetDocument;
    }

    // See https://dcdpr.github.io/did-btc1/#find-next-signals
    private static NextSignals findNextSignals(Integer contemporaryBlockheight, List<Beacon> beacons) {

        // TODO

        NextSignals nextSignals = null;

        if (log.isDebugEnabled()) log.debug("findNextSignals: " + nextSignals);
        return nextSignals;
    }

    // See https://dcdpr.github.io/did-btc1/#process-beacon-signals
    private static List<Update> processBeaconSignals(List<Signal> beaconSignals, Map<String, Object> sidecarData) {

        List<Update> updates = new ArrayList<>();

        for (Signal beaconSignal : beaconSignals) {

            String type = beaconSignal.beaconType;
            Tx signalTx = beaconSignal.tx;
            String signalId = signalTx.id;
            Map<String, Object> signalSidecarData = (Map<String, Object>) sidecarData.get(signalId);

            Update didUpdatePayload = switch(type) {
                case SingletonBeacon.TYPE -> SingletonBeacon.processSingletonBeaconSignal(signalTx, signalSidecarData);
                case CIDAggregateBeacon.TYPE -> CIDAggregateBeacon.processCIDAggregateBeaconSignal(signalTx, signalSidecarData);
                case SMTAggregateBeacon.TYPE -> SMTAggregateBeacon.processSMTAggregateBeaconSignal(signalTx, signalSidecarData);
                default -> null;
            };

            if (didUpdatePayload != null) {
                updates.add(didUpdatePayload);
            }
        }

        if (log.isDebugEnabled()) log.debug("processBeaconSignals: " + updates);
        return updates;
    }

    /*
     * Helper records
     */

    public record Beacon(Address address, String serviceEndpoint) { }
    public record NextSignals(Integer blockHeight, List<Signal> signals) { }
    public record Signal(String beaconId, String beaconType, Tx tx) { }
    public record Tx(String id) { }
    public record Update() { }

    /*
     * Getters and setters
     */

    public IPFS getIpfs() {
        return this.ipfs;
    }

    public void setIpfs(IPFS ipfs) {
        this.ipfs = ipfs;
    }
}
