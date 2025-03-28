package uniresolver.driver.did.btc1.crud.read;

import com.google.api.client.util.DateTime;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.Service;
import foundation.identity.did.validation.Validation;
import foundation.identity.jsonld.JsonLDUtils;
import io.ipfs.api.IPFS;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.AddressParser;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.uri.BitcoinURI;
import org.bitcoinj.uri.BitcoinURIParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.beacons.singleton.CIDAggregateBeacon;
import uniresolver.driver.did.btc1.beacons.singleton.SMTAggregateBeacon;
import uniresolver.driver.did.btc1.beacons.singleton.SingletonBeacon;
import uniresolver.driver.did.btc1.bitcoinconnection.BitcoinConnection;
import uniresolver.driver.did.btc1.bitcoinconnection.records.Block;
import uniresolver.driver.did.btc1.bitcoinconnection.records.Tx;
import uniresolver.driver.did.btc1.bitcoinconnection.records.TxIn;
import uniresolver.driver.did.btc1.bitcoinconnection.records.TxOut;
import uniresolver.driver.did.btc1.crud.read.records.Beacon;
import uniresolver.driver.did.btc1.crud.read.records.NextSignals;
import uniresolver.driver.did.btc1.crud.read.records.Signal;
import uniresolver.driver.did.btc1.crud.read.records.Update;
import uniresolver.driver.did.btc1.util.DIDDocumentUtil;
import uniresolver.driver.did.btc1.util.JSONPatchUtil;
import uniresolver.driver.did.btc1.util.JsonCanonicalizationAndHashUtil;
import uniresolver.driver.did.btc1.util.SHA256Util;

import java.net.URI;
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
    public DIDDocument resolveTargetDocument(DIDDocument initialDocument, Map<String, Object> resolutionOptions, Network network) throws ResolutionException {

        Integer targetVersionId = null;
        Long targetTime = null;

        if (resolutionOptions.get("versionId") != null) {
            targetVersionId = (Integer) resolutionOptions.get("versionId");
        } else if (resolutionOptions.get("versionTime") != null) {
            targetTime = DateTime.parseRfc3339((String) resolutionOptions.get("versionTime")).getValue();
        }

        Integer targetBlockheight = determineTargetBlockHeight(network, targetTime);

        Map<String, Object> signalsMetadata = resolutionOptions.get("sidecarData") == null ? null : (Map<String, Object>) ((Map<String, Object>) resolutionOptions.get("sidecarData")).get("signalsMetadata");

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
                signalsMetadata,
                network);

        if (log.isDebugEnabled()) log.debug("resolveTargetDocument: " + targetDocument);
        return targetDocument;
    }

    // See https://dcdpr.github.io/did-btc1/#determine-target-blockheight
    private static Integer determineTargetBlockHeight(Network network, Long targetTime) throws ResolutionException {

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
    private static DIDDocument traverseBlockchainHistory(DIDDocument contemporaryDIDDocument, Integer contemporaryBlockheight, Integer currentVersionId, Integer targetVersionId, Integer targetBlockheight, List<byte[]> updateHashHistory, Map<String, Object> signalsMetadata, Network network) throws ResolutionException {

        byte[] contemporaryHash = JsonCanonicalizationAndHashUtil.jsonCanonicalizationAndHash(contemporaryDIDDocument);

        List<Beacon> beacons = new ArrayList<>();
        for (Service beaconService : contemporaryDIDDocument.getServices().stream().filter(service -> Arrays.asList(SingletonBeacon.TYPE, CIDAggregateBeacon.TYPE, SMTAggregateBeacon.TYPE).contains(service.getType())).toList()) {
            URI beaconId;
            String beaconType;
            String beaconServiceEndpoint;
            Address beaconAddress;
            try {
                beaconId = beaconService.getId();
                beaconType = beaconService.getType();
                beaconServiceEndpoint = (String) beaconService.getServiceEndpoint();
                beaconAddress = BitcoinURI.of(beaconServiceEndpoint).getAddress();
            } catch (BitcoinURIParseException ex) {
                throw new ResolutionException("invalidDidDocument", "Invalid DID document: " + ex.getMessage(), ex);
            }
            beacons.add(new Beacon(beaconId, beaconType, beaconServiceEndpoint, beaconAddress));
        }

        NextSignals nextSignals = findNextSignals(contemporaryBlockheight, targetBlockheight, beacons, network);

        contemporaryBlockheight = nextSignals.blockheight();

        List<Signal> signals = nextSignals.signals();

        List<Update> updates = processBeaconSignals(signals, signalsMetadata);

        List<Update> orderedUpdates = updates.stream().sorted(Comparator.comparing(Update::targetVersionId)).toList();

        for (Update update : orderedUpdates) {
            if (update.targetVersionId() <= currentVersionId) {
                confirmDuplicateUpdate(update, updateHashHistory, contemporaryHash);
            } else if (update.targetVersionId() == currentVersionId + 1) {
                if (! Arrays.equals(update.sourceHash(), contemporaryHash)) {
                    throw new ResolutionException("latePublishing", "update.sourceHash " + Hex.encodeHexString(update.sourceHash()) + " does not match contemporaryHash: " + Hex.encodeHexString(contemporaryHash));
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

        if (Objects.equals(contemporaryBlockheight, targetBlockheight)) {
            if (log.isDebugEnabled()) log.debug("traverseBlockchainHistory: " + contemporaryDIDDocument);
            return contemporaryDIDDocument;
        }

        contemporaryBlockheight++;

        DIDDocument targetDocument = traverseBlockchainHistory(contemporaryDIDDocument, contemporaryBlockheight, currentVersionId, targetVersionId, targetBlockheight, updateHashHistory, signalsMetadata, network);

        if (log.isDebugEnabled()) log.debug("traverseBlockchainHistory: " + targetDocument);
        return targetDocument;
    }

    private static final String COINBASE_TX_IDENTIFIER = "0000000000000000000000000000000000000000000000000000000000000000";
    private static final String GENESIS_TX_IDENTIFIER = "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b";

    // See https://dcdpr.github.io/did-btc1/#find-next-signals
    private static NextSignals findNextSignals(Integer contemporaryBlockheight, Integer targetBlockheight, List<Beacon> beacons, Network network) {

        List<Signal> signals = new ArrayList<>();

        Block block = BitcoinConnection.getInstance().getBlockByBlockHeight(network, contemporaryBlockheight);

        block.txs().stream().map(Tx::txId).forEach(txId -> {
            if (COINBASE_TX_IDENTIFIER.equals(txId)) return;
            if (GENESIS_TX_IDENTIFIER.equals(txId)) return;
            Tx tx = BitcoinConnection.getInstance().getTransactionById(network, txId);
            for (TxIn txIn : tx.txIns()) {
                String prevTxId = txIn.address() /* TODO */;
                if (COINBASE_TX_IDENTIFIER.equals(prevTxId)) return;
                if (GENESIS_TX_IDENTIFIER.equals(prevTxId)) return;
                Tx prevTx = BitcoinConnection.getInstance().getTransactionById(network, prevTxId);
                TxOut spentTxOut = prevTx.txOuts().get(0 /* TODO txIn.prevIndex */);
                Address spentAddress = /* TODO */ AddressParser.getDefault(network.toBitcoinjNetwork()).parseAddress(spentTxOut.address());
                Optional<Beacon> foundBeacon = beacons.stream().filter(beacon -> beacon.address().equals(/* TODO */ spentAddress)).findAny();
                if (foundBeacon.isPresent()) {
                    Signal beaconSignal = new Signal(JsonLDUtils.uriToString(foundBeacon.get().id()), foundBeacon.get().type(), tx);
                    signals.add(beaconSignal);
                    break;
                }
            }
        });

        NextSignals nextSignals;

        if (Objects.equals(contemporaryBlockheight, targetBlockheight)) {
            nextSignals = new NextSignals(contemporaryBlockheight, signals);
        } else if (signals.isEmpty()) {
            nextSignals = findNextSignals(contemporaryBlockheight + 1, targetBlockheight, beacons, network);
        } else {
            nextSignals = new NextSignals(contemporaryBlockheight, signals);
        }

        if (log.isDebugEnabled()) log.debug("findNextSignals: " + nextSignals);
        return nextSignals;
    }

    // See https://dcdpr.github.io/did-btc1/#process-beacon-signals
    private static List<Update> processBeaconSignals(List<Signal> beaconSignals, Map<String, Object> signalsMetadata) {

        List<Update> updates = new ArrayList<>();

        for (Signal beaconSignal : beaconSignals) {

            String type = beaconSignal.beaconType();
            Tx signalTx = beaconSignal.tx();
            String signalId = signalTx.txId();
            Map<String, Object> signalSidecarData = (Map<String, Object>) signalsMetadata.get(signalId);

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
     * Getters and setters
     */

    public IPFS getIpfs() {
        return this.ipfs;
    }

    public void setIpfs(IPFS ipfs) {
        this.ipfs = ipfs;
    }
}
