package uniresolver.driver.did.btc1.crud.read;

import com.google.api.client.util.DateTime;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.Service;
import foundation.identity.did.validation.Validation;
import io.ipfs.api.IPFS;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.base.Address;
import org.bitcoinj.uri.BitcoinURI;
import org.bitcoinj.uri.BitcoinURIParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btc1.beacons.singleton.CIDAggregateBeacon;
import uniresolver.driver.did.btc1.beacons.singleton.SMTAggregateBeacon;
import uniresolver.driver.did.btc1.beacons.singleton.SingletonBeacon;
import uniresolver.driver.did.btc1.util.DIDDocumentUtil;
import uniresolver.driver.did.btc1.util.JSONPatchUtil;
import uniresolver.driver.did.btc1.util.JsonCanonicalizationAndHashUtil;
import uniresolver.driver.did.btc1.util.SHA256Util;

import java.nio.charset.StandardCharsets;
import java.util.*;

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

        List<byte[]> updateHashHistory = new ArrayList<>();

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
    private static DIDDocument traverseBlockchainHistory(DIDDocument contemporaryDIDDocument, Integer contemporaryBlockheight, Integer currentVersionId, Integer targetVersionId, Integer targetBlockheight, List<byte[]> updateHashHistory, Map<String, Object> sidecarData) throws ResolutionException {

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

        List<Update> orderedUpdates = updates.stream().sorted(Comparator.comparing(Update::targetVersionId)).toList();

        for (Update update : orderedUpdates) {
            if (update.targetVersionId() <= currentVersionId) {
                confirmDuplicateUpdate(update, updateHashHistory, contemporaryHash);
            } else if (update.targetVersionId() == currentVersionId + 1) {
                if (! Arrays.equals(update.sourceHash, contemporaryHash)) {
                    throw new ResolutionException("latePublishing", "update.sourceHash " + Hex.encodeHexString(update.sourceHash) + " does not match contemporaryHash: " + Hex.encodeHexString(contemporaryHash));
                }
                contemporaryDIDDocument = applyDIDUpdate(contemporaryDIDDocument, update);
                currentVersionId++;
                if (currentVersionId.equals(targetVersionId)) {
                    return contemporaryDIDDocument;
                }
                byte[] updateHash = JsonCanonicalizationAndHashUtil.jsonCanonicalizationAndHash(update);
                updateHashHistory.add(updateHash);
                contemporaryHash = JsonCanonicalizationAndHashUtil.jsonCanonicalizationAndHash(contemporaryDIDDocument);
            } else if (update.targetVersionId() > currentVersionId + 1) {
                throw new ResolutionException("latePublishing", "update.targetVersionId " + update.targetVersionId() + " is greater than currentVersionId + 1: " + (currentVersionId + 1));
            }
        }

        contemporaryBlockheight++;

        DIDDocument targetDocument = traverseBlockchainHistory(contemporaryDIDDocument, contemporaryBlockheight, currentVersionId, targetVersionId, targetBlockheight, updateHashHistory, sidecarData);

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

    // See https://dcdpr.github.io/did-btc1/#confirm-duplicate-update
    private static void confirmDuplicateUpdate(Update update, List<byte[]> updateHashHistory, byte[] contemporaryHash) throws ResolutionException {

        byte[] updateHash = JsonCanonicalizationAndHashUtil.jsonCanonicalizationAndHash(update);
        Integer updateHashIndex = update.targetVersionId() - 2;
        byte[] historicalUpdateHash = updateHashHistory.get(updateHashIndex);
        if (! Arrays.equals(historicalUpdateHash, updateHash)) {
            throw new ResolutionException("latePublishing", "historicalUpdateHash " + Hex.encodeHexString(historicalUpdateHash) + " does not match updateHash: " + Hex.encodeHexString(updateHash));
        }
    }

    // See https://dcdpr.github.io/did-btc1/#apply-did-update
    private static DIDDocument applyDIDUpdate(DIDDocument contemporaryDIDDocument, Update update) throws ResolutionException {

/*        DataIntegrityProof dataIntegrityProof = DataIntegrityProof.builder()
                .type(DataIntegritySuites.DATA_INTEGRITY_SUITE_DATAINTEGRITYPROOF.getTerm())
                .cryptosuite("bip340-rdfc-2025")*/

        DIDDocument targetDIDDocument = DIDDocumentUtil.copy(contemporaryDIDDocument);
        targetDIDDocument = JSONPatchUtil.apply(targetDIDDocument, update.patch());
        Validation.validate(targetDIDDocument);
        byte[] targetHash = SHA256Util.sha256(targetDIDDocument.toJson().getBytes(StandardCharsets.UTF_8));
        if (! Arrays.equals(targetHash, update.targetHash())) {
            throw new ResolutionException("invalidDidUpdate", "targetHash " + Hex.encodeHexString(targetHash) + " does not match update.targetHash: " + Hex.encodeHexString(update.targetHash()));
        }

        if (log.isDebugEnabled()) log.debug("applyDIDUpdate: " + targetDIDDocument);
        return targetDIDDocument;
    }

    /*
     * Helper records
     */

    public record Beacon(Address address, String serviceEndpoint) { }
    public record NextSignals(Integer blockHeight, List<Signal> signals) { }
    public record Signal(String beaconId, String beaconType, Tx tx) { }
    public record Tx(String id) { }
    public record Update(Integer targetVersionId, byte[] sourceHash, byte[] targetHash, String patch) { }

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
