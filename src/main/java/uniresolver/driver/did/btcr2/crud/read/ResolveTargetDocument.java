package uniresolver.driver.did.btcr2.crud.read;

import com.danubetech.dataintegrity.DataIntegrityProof;
import com.danubetech.dataintegrity.suites.DataIntegritySuite;
import com.danubetech.dataintegrity.suites.DataIntegritySuites;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.Service;
import foundation.identity.jsonld.JsonLDUtils;
import io.leonard.AddressFormatException;
import io.leonard.Base58;
import org.bitcoinj.base.Address;
import org.bitcoinj.uri.BitcoinURI;
import org.bitcoinj.uri.BitcoinURIParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btcr2.Network;
import uniresolver.driver.did.btcr2.appendix.JsonCanonicalizationAndHash;
import uniresolver.driver.did.btcr2.appendix.RootDidBtcr2UpdateCapabilities;
import uniresolver.driver.did.btcr2.beacons.singleton.CIDAggregateBeacon;
import uniresolver.driver.did.btcr2.beacons.singleton.SMTAggregateBeacon;
import uniresolver.driver.did.btcr2.beacons.singleton.SingletonBeacon;
import uniresolver.driver.did.btcr2.connections.bitcoin.BitcoinConnector;
import uniresolver.driver.did.btcr2.connections.bitcoin.records.Block;
import uniresolver.driver.did.btcr2.connections.bitcoin.records.Tx;
import uniresolver.driver.did.btcr2.connections.ipfs.IPFSConnection;
import uniresolver.driver.did.btcr2.crud.read.records.Beacon;
import uniresolver.driver.did.btcr2.crud.read.records.BeaconSignal;
import uniresolver.driver.did.btcr2.crud.update.jsonld.DIDUpdate;
import uniresolver.driver.did.btcr2.crud.update.jsonld.RootCapability;
import uniresolver.driver.did.btcr2.dataintegrity.DataIntegrity;
import uniresolver.driver.did.btcr2.util.HexUtil;
import uniresolver.driver.did.btcr2.util.JSONPatchUtil;
import uniresolver.driver.did.btcr2.util.JsonLDUtil;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class ResolveTargetDocument {

    private static final Logger log = LoggerFactory.getLogger(ResolveTargetDocument.class);

    private Read read;
    private BitcoinConnector bitcoinConnector;
    private IPFSConnection ipfsConnection;

    public ResolveTargetDocument(Read read, BitcoinConnector bitcoinConnector, IPFSConnection ipfsConnection) {
        this.read = read;
        this.bitcoinConnector = bitcoinConnector;
        this.ipfsConnection = ipfsConnection;
    }

    /*
     * 7.2.2 Resolve Target Document
     */

    // See https://dcdpr.github.io/did-btcr2/#resolve-target-document
    public DIDDocument resolveTargetDocument(DIDDocument initialDocument, Map<String, Object> resolutionOptions, Network network, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) throws ResolutionException {
        if (log.isDebugEnabled()) log.debug("resolveTargetDocument ({}, {}, {})", initialDocument, resolutionOptions, network);

        Integer targetVersionId;
        Long targetTime;

        if (resolutionOptions.get("versionId") != null) {

            // If resolutionOptions.versionId is not null, set targetVersionId to resolutionOptions.versionId.

            targetVersionId = (Integer) resolutionOptions.get("versionId");
            targetTime = null;
        } else if (resolutionOptions.get("versionTime") != null) {

            // Else if resolutionOptions.versionTime is not null, set targetTime to resolutionOptions.versionTime.

            targetVersionId = null;
            targetTime = ((Number) resolutionOptions.get("versionTime")).longValue();
        } else {

            // Else set targetTime to the UNIX timestamp for now at the moment of execution.

            targetVersionId = null;
            targetTime = System.currentTimeMillis();
        }

        // Set signalsMetadata to resolutionOptions.sidecarData.signalsMetadata.

        Map<String, Object> sidecarData = resolutionOptions.get("sidecarData") == null ? null : (Map<String, Object>) resolutionOptions.get("sidecarData");
        Map<String, Object> signalsMetadata = sidecarData == null ? null : (Map<String, Object>) sidecarData.get("signalsMetadata");

        // Set currentVersionId to 1.

        Integer currentVersionId = 1;

        didDocumentMetadata.put("versionId", currentVersionId);

        // If currentVersionId equals targetVersionId return initialDocument.

        if (currentVersionId.equals(targetVersionId)) return initialDocument;

        // Set updateHashHistory to an empty array.

        List<byte[]> updateHashHistory = new ArrayList<>();

        // Set didDocumentHistory to an array containing the initialDocument.

        List<DIDDocument> didDocumentHistory = new ArrayList<>(List.of(initialDocument));

        // Set contemporaryBlockheight to 0.

        Integer contemporaryBlockheight = 0;

        // Set contemporaryDIDDocument to the initialDocument.

        DIDDocument contemporaryDIDDocument = initialDocument;

        // Set targetDocument to the result of calling the Traverse Blockchain History algorithm passing in
        // contemporaryDIDDocument, contemporaryBlockheight, currentVersionId, targetVersionId, targetTime,
        // updateHashHistory, signalsMetadata, and network.

        DIDDocument targetDocument = this.traverseBlockchainHistory(
                contemporaryDIDDocument,
                contemporaryBlockheight,
                currentVersionId,
                targetVersionId,
                targetTime,
                didDocumentHistory,
                updateHashHistory,
                signalsMetadata,
                network,
                didDocumentMetadata);

        if (log.isDebugEnabled()) log.debug("resolveTargetDocument: " + targetDocument);
        return targetDocument;
    }

    private static final Integer MIN_CONFIRMATIONS = 10; /* TODO: what is X. Is it variable? */

    // See https://dcdpr.github.io/did-btcr2/#traverse-blockchain-history
    private DIDDocument traverseBlockchainHistory(DIDDocument contemporaryDIDDocument, Integer contemporaryBlockheight, Integer currentVersionId, Integer targetVersionId, Long targetTime, List<DIDDocument> didDocumentHistory, List<byte[]> updateHashHistory, Map<String, Object> signalsMetadata, Network network, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) throws ResolutionException {
        if (log.isDebugEnabled()) log.debug("traverseBlockchainHistory ({}, {}, {}, {}, {}, {}, {}, {}, {})", contemporaryDIDDocument, contemporaryBlockheight, currentVersionId, targetVersionId, targetTime, didDocumentHistory, updateHashHistory, signalsMetadata, network);

        // Set contemporaryHash to the result of passing contemporaryDIDDocument into the JSON Canonicalization and Hash algorithm.

        byte[] contemporaryHash = JsonCanonicalizationAndHash.jsonCanonicalizationAndHash(contemporaryDIDDocument);

        // Find all beacons in contemporaryDIDDocument: All service in contemporaryDIDDocument.service where service.type equals
        // one of SingletonBeacon, CIDAggregateBeacon and SMTAggregateBeacon Beacon.

        List<Service> beaconServices = contemporaryDIDDocument.getServices().stream().filter(service -> Arrays.asList(SingletonBeacon.TYPE, CIDAggregateBeacon.TYPE, SMTAggregateBeacon.TYPE).contains(service.getType())).toList();

        // For each beacon in beacons convert the beacon.serviceEndpoint to a Bitcoin address following BIP21.
        // Set beacon.address to the Bitcoin address.

        List<Beacon> beacons = new ArrayList<>();
        for (Service beaconService : beaconServices) {
            try {
                String id = JsonLDUtils.uriToString(beaconService.getId());
                String type = beaconService.getType();
                String serviceEndpoint = beaconService.getServiceEndpoint() instanceof URI ? JsonLDUtils.uriToString((URI) beaconService.getServiceEndpoint()) : (String) beaconService.getServiceEndpoint();
                Address address = BitcoinURI.of(serviceEndpoint).getAddress();
                beacons.add(new Beacon(
                        id,
                        type,
                        serviceEndpoint,
                        address));
            } catch (BitcoinURIParseException ex) {
                throw new ResolutionException("invalidDidDocument", "Invalid DID document: " + ex.getMessage(), ex);
            }
        }

        // Set nextSignals to the result of calling algorithm Find Next Signals passing in
        // contemporaryBlockheight, beacons and network.

        List<BeaconSignal> nextSignals = this.findNextSignals(contemporaryBlockheight, beacons, network, didDocumentMetadata);

        // If nextSignals is empty, return contemporaryDIDDocument.

        if (nextSignals.isEmpty()) return contemporaryDIDDocument;

        // If nextSignals[0].blocktime is greater than targetTime, return contemporaryDIDDocument.

        if (nextSignals.get(0).blocktime() > targetTime) return contemporaryDIDDocument;

        // Set contemporaryBlockheight to nextSignals[0].blockheight.

        contemporaryBlockheight = nextSignals.get(0).blockheight();

        // Set updates to the result of calling algorithm Process Beacon Signals passing in nextSignals and signalsMetadata.

        List<DIDUpdate> updates = this.processBeaconSignals(nextSignals, signalsMetadata, didDocumentMetadata);

        // Set orderedUpdates to the list of updates ordered by the targetVersionId property.

        List<DIDUpdate> orderedUpdates = updates.stream().sorted(Comparator.comparing(DIDUpdate::getTargetVersionId)).toList();

        // For update in orderedUpdates:

        for (DIDUpdate update : orderedUpdates) {

            // If update.targetVersionId is less than or equal to currentVersionId, run the
            // Confirm Duplicate Update Algorithm passing in update, updateHashHistory, and contemporaryHash.

            if (update.getTargetVersionId() <= currentVersionId) {
                confirmDuplicateUpdate(update, updateHashHistory, contemporaryHash);
            }

            // If update.targetVersionId equals currentVersionId + 1:

            if (update.getTargetVersionId() == currentVersionId + 1) {

                // Check that the base58 decoding of update.sourceHash equals contemporaryHash, else MUST raise latePublishing error.

                try {
                    if (!Arrays.equals(Base58.decode(update.getSourceHash()), contemporaryHash)) {
                        throw new ResolutionException("latePublishing", "update.sourceHash " + update.getSourceHash() + " does not match contemporaryHash: " + Base58.encode(contemporaryHash));
                    }
                } catch (AddressFormatException ex) {
                    throw new ResolutionException("invalidDidUpdate", "Update sourceHash " + update.getSourceHash() + " cannot be decoded: " + ex.getMessage());
                }

                // Set contemporaryDIDDocument to the result of calling Apply DID Update algorithm passing
                // in contemporaryDIDDocument, update.

                contemporaryDIDDocument = applyDIDUpdate(contemporaryDIDDocument, update);

                // Push contemporaryDIDDocument onto didDocumentHistory.

                didDocumentHistory.add(contemporaryDIDDocument);

                // Increment currentVersionId

                currentVersionId++;

                didDocumentMetadata.put("versionId", currentVersionId);

                // If currentVersionId equals targetVersionId return contemporaryDIDDocument.

/*                if (currentVersionId.equals(targetVersionId)) {
                    return contemporaryDIDDocument;
                }*/

                // Set unsecuredUpdate to a copy of the update object.

                DIDUpdate unsecuredUpdate = JsonLDUtil.copy(update, DIDUpdate.class);

                // Remove the proof property from the unsecuredUpdate object.

                JsonLDUtils.jsonLdRemove(unsecuredUpdate, "proof");

                // Set updateHash to the result of passing unsecuredUpdate into the JSON Canonicalization and Hash algorithm

                byte[] updateHash = JsonCanonicalizationAndHash.jsonCanonicalizationAndHash(unsecuredUpdate);

                // Push updateHash onto updateHashHistory.

                updateHashHistory.add(updateHash);

                // Set contemporaryHash to result of passing contemporaryDIDDocument into the
                // JSON Canonicalization and Hash algorithm.

                contemporaryHash = JsonCanonicalizationAndHash.jsonCanonicalizationAndHash(contemporaryDIDDocument);
            }

            // If update.targetVersionId is greater than currentVersionId + 1, MUST throw a LatePublishing error.

            if (update.getTargetVersionId() > currentVersionId + 1) {
                throw new ResolutionException("latePublishing", "update.targetVersionId " + update.getTargetVersionId() + " is greater than currentVersionId + 1: " + (currentVersionId + 1));
            }
        }

        // Increment contemporaryBlockheight.

        contemporaryBlockheight++;

        // Set targetDocument to the result of calling the Traverse Blockchain History algorithm passing in
        // contemporaryDIDDocument, contemporaryBlockheight, currentVersionId, targetVersionId, targetTime,
        // didDocumentHistory, updateHashHistory, signalsMetadata, and network.

        DIDDocument targetDocument = this.traverseBlockchainHistory(
                contemporaryDIDDocument,
                contemporaryBlockheight,
                currentVersionId,
                targetVersionId,
                targetTime,
                didDocumentHistory,
                updateHashHistory,
                signalsMetadata,
                network,
                didDocumentMetadata);

        // Return targetDocument.

        if (log.isDebugEnabled()) log.debug("traverseBlockchainHistory: " + targetDocument);
        return targetDocument;
    }

    private static final Pattern PATTERN_TXOUT = Pattern.compile("^OP_RETURN OP_PUSHBYTES_32 ([0-9a-fA-F]{64})$");

    // See https://dcdpr.github.io/did-btcr2/#find-next-signals
    private List<BeaconSignal> findNextSignals(Integer contemporaryBlockheight, List<Beacon> beacons, Network network, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) {
        if (log.isDebugEnabled()) log.debug("findNextSignals ({}, {}, {})", contemporaryBlockheight, beacons, network);

        // Set signals to an empty array.

        List<BeaconSignal> beaconSignals = new ArrayList<>();

        // For each beacon in beacons:

        for (Beacon beacon : beacons) {

            if (log.isDebugEnabled()) log.debug("Processing beacon: " + beacon);

            // Set beaconSpends to the set of all Bitcoin transactions on the specified network that spend
            // at least one transaction input controlled by the beacon.address with a blockheight greater than or equal to the contemporaryBlockheight.

            List<Tx> beaconSpends = this.getBitcoinConnector().getBitcoinConnection(network).getAddressTransactions(beacon.address());
            Map<Tx, Block> beaconSpendsBlocks = new LinkedHashMap<>();
            beaconSpends.forEach(tx -> {
                beaconSpendsBlocks.put(tx, bitcoinConnector.getBitcoinConnection(network).getBlockByTransaction(tx.txId()));
            });
            beaconSpends = beaconSpends.stream().filter(tx -> beaconSpendsBlocks.get(tx).blockHeight() >= contemporaryBlockheight).toList();

            // Filter the beaconSpends, identifying all transactions whose 0th transaction output
            // is of the format [OP_RETURN, OP_PUSH32, <32bytes>].
            // TODO: This is incorrect, should be last instead of 0th?

            beaconSpends = beaconSpends.stream().filter(tx -> PATTERN_TXOUT.matcher(tx.txOuts().getLast().asm()).matches()).toList();

            // For each of the filtered beaconSpends push the following beaconSignal object onto the signals array.

            for (Tx beaconSpend : beaconSpends) {

                if (log.isDebugEnabled()) log.debug("Processing beaconSpend: " + beaconSpend);

                BeaconSignal beaconSignal = new BeaconSignal(
                        beacon.id(),
                        beacon.type(),
                        beaconSpend,
                        beaconSpendsBlocks.get(beaconSpend).blockHeight(),
                        beaconSpendsBlocks.get(beaconSpend).blockTime());

                beaconSignals.add(beaconSignal);

                List<Map<String, Object>> didDocumentMetadataSignals = (List<Map<String, Object>>) didDocumentMetadata.computeIfAbsent("signals", x -> new ArrayList<>());
                Map<String, Object> didDocumentMetadataSignal = new LinkedHashMap<>();
                didDocumentMetadataSignals.add(didDocumentMetadataSignal);
                didDocumentMetadataSignal.put("signalId", beaconSpend.txId());
                didDocumentMetadataSignal.put("blockheight", beaconSpendsBlocks.get(beaconSpend).blockHeight());
            }
        }

        // If signals is empty, return signals.

        if (beaconSignals.isEmpty()) return beaconSignals;

        // Sort signals by blockheight from lowest to highest.

        List<BeaconSignal> orderedBeaconSignals = beaconSignals.stream().sorted(Comparator.comparing(BeaconSignal::blockheight)).toList();

        // Set nextSignals to all signals with the lowest blockheight.

        Integer lowestBlockHeight = orderedBeaconSignals.getFirst().blockheight();
        List<BeaconSignal> nextSignals = orderedBeaconSignals.stream().filter(x -> Objects.equals(x.blockheight(), lowestBlockHeight)).toList();

        // Return nextSignals.

        if (log.isDebugEnabled()) log.debug("findNextSignals: " + nextSignals);
        return nextSignals;
    }

    // See https://dcdpr.github.io/did-btcr2/#process-beacon-signals
    private List<DIDUpdate> processBeaconSignals(List<BeaconSignal> beaconSignals, Map<String, Object> signalsMetadata, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) throws ResolutionException {
        if (log.isDebugEnabled()) log.debug("processBeaconSignals ({}, {})", beaconSignals, signalsMetadata);

        // Set updates to an empty array.

        List<DIDUpdate> updates = new ArrayList<>();

        // For beaconSignal in beaconSignals:

        for (BeaconSignal beaconSignal : beaconSignals) {

            // Set type to beaconSignal.beaconType.

            String type = beaconSignal.beaconType();

            // Set signalTx to beaconSignal.tx.

            Tx signalTx = beaconSignal.tx();

            // Set signalId to signalTx.id.

            String signalId = signalTx.txId();

            // Set signalSidecarData to signalsMetadata[signalId]. TODO: formalize structure of sidecarData

            Map<String, Object> signalSidecarData = signalsMetadata == null ? null : (Map<String, Object>) signalsMetadata.get(signalId);

            // Set didUpdatePayload to null.

            DIDUpdate didUpdate = switch(type) {

                // If type == SingletonBeacon:
                //    Set didUpdatePayload to the result of passing signalTx and
                //    signalSidecarData to the Process Singleton Beacon Signal algorithm.

                case SingletonBeacon.TYPE -> SingletonBeacon.processSingletonBeaconSignal(signalTx, signalSidecarData, this.getIpfsConnection(), didDocumentMetadata);

                // If type == CIDAggregateBeacon:
                //    Set didUpdatePayload to the result of passing signalTx and
                //    signalSidecarData to the Process CIDAggregate Beacon Signal algorithm.

                case CIDAggregateBeacon.TYPE -> CIDAggregateBeacon.processCIDAggregateBeaconSignal(signalTx, signalSidecarData);

                // If type == SMTAggregateBeacon:
                //    Set didUpdatePayload to the result of passing signalTx and
                //    signalSidecarData to the Process SMTAggregate Beacon Signal algorithm.

                case SMTAggregateBeacon.TYPE -> SMTAggregateBeacon.processSMTAggregateBeaconSignal(signalTx, signalSidecarData);

                default -> null;
            };

            // If didUpdatePayload is not null, push didUpdatePayload to updates.

            if (didUpdate != null) updates.add(didUpdate);
        }

        // Return updates.

        if (log.isDebugEnabled()) log.debug("processBeaconSignals: " + updates);
        return updates;
    }

    // See https://dcdpr.github.io/did-btcr2/#confirm-duplicate-update
    private static void confirmDuplicateUpdate(DIDUpdate update, List<byte[]> updateHashHistory, byte[] contemporaryHash) throws ResolutionException {
        if (log.isDebugEnabled()) log.debug("confirmDuplicateUpdate ({}, {}, {})", update, updateHashHistory, contemporaryHash);

        DIDUpdate unsecuredUpdate = JsonLDUtil.copy(update, DIDUpdate.class);
        JsonLDUtils.jsonLdRemove(unsecuredUpdate, "proof");
        byte[] updateHash = JsonCanonicalizationAndHash.jsonCanonicalizationAndHash(unsecuredUpdate);
        Integer updateHashIndex = update.getTargetVersionId() - 2;
        byte[] historicalUpdateHash = updateHashHistory.get(updateHashIndex);
        if (! Arrays.equals(historicalUpdateHash, updateHash)) {
            throw new ResolutionException("latePublishing", "historicalUpdateHash " + HexUtil.hexEncode(historicalUpdateHash) + " does not match updateHash: " + HexUtil.hexEncode(updateHash));
        }
    }

    // See https://dcdpr.github.io/did-btcr2/#apply-did-update
    private static DIDDocument applyDIDUpdate(DIDDocument contemporaryDIDDocument, DIDUpdate update) throws ResolutionException {
        if (log.isDebugEnabled()) log.debug("applyDIDUpdate ({}, {})", contemporaryDIDDocument, update);

        DataIntegrityProof dataIntegrityProof = DataIntegrityProof.getFromJsonLDObject(update);
        String capabilityId = JsonLDUtils.jsonLdGetString(dataIntegrityProof.getJsonObject(), "capability");
        if (capabilityId == null) throw new ResolutionException(ResolutionException.ERROR_INVALID_DID, "No 'capability' found in update proof: " + update);

        RootCapability rootCapability = RootDidBtcr2UpdateCapabilities.dereferenceRootCapabilityIdentifier(capabilityId);

        if (! rootCapability.getInvocationTarget().equals(contemporaryDIDDocument.getId())) {
            throw new ResolutionException("invalidDidUpdate", "Root capability 'invocationTarget' " + rootCapability.getInvocationTarget() + " does not match contemporary DID document 'id': " + contemporaryDIDDocument.getId());
        }
        if (! rootCapability.getController().equals(contemporaryDIDDocument.getId())) {
            throw new ResolutionException("invalidDidUpdate", "Root capability 'controller' " + rootCapability.getInvocationTarget() + " does not match contemporary DID document 'id': " + contemporaryDIDDocument.getId());
        }

        DataIntegritySuite cryptosuite = DataIntegritySuites.DATA_INTEGRITY_SUITE_DATAINTEGRITYPROOF;

        String expectedProofPurpose = "capabilityInvocation";

        String mediaType = "application/json";

        byte[] documentBytes = /* TODO */ update.toJson().getBytes(StandardCharsets.UTF_8);

        boolean verificationResult = DataIntegrity.verifyProofAlgorithm(mediaType, documentBytes, cryptosuite, expectedProofPurpose, /* TODO: extra, not in spec */ update, dataIntegrityProof, contemporaryDIDDocument);

        if (! verificationResult) {
            throw new ResolutionException("invalidUpdateProof", "Update payload could not be verified: " + update);
        }

        DIDDocument targetDIDDocument = JsonLDUtil.copy(contemporaryDIDDocument, DIDDocument.class);
        targetDIDDocument = JSONPatchUtil.apply(targetDIDDocument, update.getPatch());
        try {
            // TODO: Validation.validate(targetDIDDocument);
        } catch (Exception ex) {
            throw new ResolutionException("invalidDidDocument", "Invalid target DID document: " + ex.getMessage(), ex);
        }

        byte[] targetHash = JsonCanonicalizationAndHash.jsonCanonicalizationAndHash(targetDIDDocument);
        try {
            if (! Arrays.equals(targetHash, Base58.decode(update.getTargetHash()))) {
                throw new ResolutionException("invalidDidUpdate", "targetHash " + Base58.encode(targetHash) + " does not match update.targetHash: " + update.getTargetHash());
            }
        } catch (AddressFormatException ex) {
            throw new ResolutionException("invalidDidUpdate", "Update targetHash " + update.getTargetHash() + " cannot be decoded: " + ex.getMessage());
        }

        if (log.isDebugEnabled()) log.debug("applyDIDUpdate: " + targetDIDDocument);
        return targetDIDDocument;
    }

    /*
     * Getters and setters
     */

    public Read getRead() {
        return read;
    }

    public void setRead(Read read) {
        this.read = read;
    }

    public BitcoinConnector getBitcoinConnector() {
        return bitcoinConnector;
    }

    public void setBitcoinConnector(BitcoinConnector bitcoinConnector) {
        this.bitcoinConnector = bitcoinConnector;
    }

    public IPFSConnection getIpfsConnection() {
        return ipfsConnection;
    }

    public void setIpfsConnection(IPFSConnection ipfsConnection) {
        this.ipfsConnection = ipfsConnection;
    }
}
