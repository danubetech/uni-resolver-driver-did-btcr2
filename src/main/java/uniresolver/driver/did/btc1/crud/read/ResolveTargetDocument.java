package uniresolver.driver.did.btc1.crud.read;

import com.google.api.client.util.DateTime;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.Service;
import foundation.identity.did.validation.Validation;
import foundation.identity.jsonld.JsonLDUtils;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.AddressParser;
import org.bitcoinj.uri.BitcoinURI;
import org.bitcoinj.uri.BitcoinURIParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.appendix.JsonCanonicalizationAndHash;
import uniresolver.driver.did.btc1.beacons.singleton.CIDAggregateBeacon;
import uniresolver.driver.did.btc1.beacons.singleton.SMTAggregateBeacon;
import uniresolver.driver.did.btc1.beacons.singleton.SingletonBeacon;
import uniresolver.driver.did.btc1.connections.bitcoin.BitcoinConnection;
import uniresolver.driver.did.btc1.connections.bitcoin.records.Block;
import uniresolver.driver.did.btc1.connections.bitcoin.records.Tx;
import uniresolver.driver.did.btc1.connections.bitcoin.records.TxIn;
import uniresolver.driver.did.btc1.connections.bitcoin.records.TxOut;
import uniresolver.driver.did.btc1.connections.ipfs.IPFSConnection;
import uniresolver.driver.did.btc1.crud.read.records.Beacon;
import uniresolver.driver.did.btc1.crud.read.records.NextSignals;
import uniresolver.driver.did.btc1.crud.read.records.Signal;
import uniresolver.driver.did.btc1.crud.update.records.DIDUpdatePayload;
import uniresolver.driver.did.btc1.util.DIDDocumentUtil;
import uniresolver.driver.did.btc1.util.HexUtil;
import uniresolver.driver.did.btc1.util.JSONPatchUtil;
import uniresolver.driver.did.btc1.util.RecordUtil;

import java.net.URI;
import java.util.*;

public class ResolveTargetDocument {

    private static final Logger log = LoggerFactory.getLogger(ResolveTargetDocument.class);

    private BitcoinConnection bitcoinConnection;
    private IPFSConnection ipfsConnection;

    public ResolveTargetDocument(BitcoinConnection bitcoinConnection, IPFSConnection ipfsConnection) {
        this.bitcoinConnection = bitcoinConnection;
        this.ipfsConnection = ipfsConnection;
    }

    /*
     * 4.2.3 Resolve Target Document
     */

    // See https://dcdpr.github.io/did-btc1/#resolve-target-document
    public DIDDocument resolveTargetDocument(DIDDocument initialDocument, Map<String, Object> resolutionOptions, Network network, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) throws ResolutionException {
        if (log.isDebugEnabled()) log.debug("resolveTargetDocument ({}, {}, {})", initialDocument, resolutionOptions, network);

        Integer targetVersionId = null;
        Long targetTime = null;

        if (resolutionOptions.get("versionId") != null) {
            targetVersionId = (Integer) resolutionOptions.get("versionId");
        } else if (resolutionOptions.get("versionTime") != null) {
            targetTime = DateTime.parseRfc3339((String) resolutionOptions.get("versionTime")).getValue();
        }

        Integer targetBlockheight = this.determineTargetBlockHeight(network, targetTime);

        Map<String, Object> signalsMetadata = resolutionOptions.get("sidecarData") == null ? null : (Map<String, Object>) ((Map<String, Object>) resolutionOptions.get("sidecarData")).get("signalsMetadata");

        Integer currentVersionId = 1;
        if (currentVersionId.equals(targetVersionId)) return initialDocument;

        List<byte[]> updateHashHistory = new ArrayList<>();

        Integer contemporaryBlockheight = 0;

        DIDDocument contemporaryDIDDocument = initialDocument;

        DIDDocument targetDocument = this.traverseBlockchainHistory(
                contemporaryDIDDocument,
                contemporaryBlockheight,
                currentVersionId,
                targetVersionId,
                targetBlockheight,
                updateHashHistory,
                signalsMetadata,
                network,
                didDocumentMetadata);

        if (log.isDebugEnabled()) log.debug("resolveTargetDocument: " + targetDocument);
        return targetDocument;
    }

    private static final Integer MIN_CONFIRMATIONS = 10; /* TODO: what is X. Is it variable? */

    // See https://dcdpr.github.io/did-btc1/#determine-target-blockheight
    private Integer determineTargetBlockHeight(Network network, Long targetTime) throws ResolutionException {
        if (log.isDebugEnabled()) log.debug("determineTargetBlockHeight ({}, {})", network, targetTime);

        Block block;

        if (targetTime != null) {
            block = this.getBitcoinConnection().getBlockByTargetTime(network, targetTime);
        } else {
            block = this.getBitcoinConnection().getBlockByMinConfirmations(network, MIN_CONFIRMATIONS);
        }

        Integer blockHeight = block.blockHeight();

        if (log.isDebugEnabled()) log.debug("determineTargetBlockHeight: " + blockHeight);
        return blockHeight;
    }

    // See https://dcdpr.github.io/did-btc1/#traverse-blockchain-history
    private DIDDocument traverseBlockchainHistory(DIDDocument contemporaryDIDDocument, Integer contemporaryBlockheight, Integer currentVersionId, Integer targetVersionId, Integer targetBlockheight, List<byte[]> updateHashHistory, Map<String, Object> signalsMetadata, Network network, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) throws ResolutionException {
        if (log.isDebugEnabled()) log.debug("traverseBlockchainHistory ({}, {}, {}, {}, {}, {}, {}, {})", contemporaryDIDDocument, contemporaryBlockheight, currentVersionId, targetVersionId, targetBlockheight, updateHashHistory, signalsMetadata, network);

        byte[] contemporaryHash = JsonCanonicalizationAndHash.jsonCanonicalizationAndHash(contemporaryDIDDocument);

        List<Beacon> beacons = new ArrayList<>();
        for (Service beaconService : contemporaryDIDDocument.getServices().stream().filter(service -> Arrays.asList(SingletonBeacon.TYPE, CIDAggregateBeacon.TYPE, SMTAggregateBeacon.TYPE).contains(service.getType())).toList()) {
            String beaconId;
            String beaconType;
            String beaconServiceEndpoint;
            Address beaconAddress;
            try {
                beaconId = JsonLDUtils.uriToString(beaconService.getId());
                beaconType = beaconService.getType();
                beaconServiceEndpoint = beaconService.getServiceEndpoint() instanceof URI ? JsonLDUtils.uriToString((URI) beaconService.getServiceEndpoint()) : (String) beaconService.getServiceEndpoint();
                beaconAddress = BitcoinURI.of(beaconServiceEndpoint).getAddress();
            } catch (BitcoinURIParseException ex) {
                throw new ResolutionException("invalidDidDocument", "Invalid DID document: " + ex.getMessage(), ex);
            }
            beacons.add(new Beacon(beaconId, beaconType, beaconServiceEndpoint, beaconAddress));
        }

        NextSignals nextSignals = this.findNextSignals(contemporaryBlockheight, targetBlockheight, beacons, network, didDocumentMetadata);

        contemporaryBlockheight = nextSignals.blockheight();

        List<Signal> signals = nextSignals.signals();

        List<DIDUpdatePayload> didUpdatePayloads = this.processBeaconSignals(signals, signalsMetadata, didDocumentMetadata);

        List<DIDUpdatePayload> orderedDidUpdatePayloads = didUpdatePayloads.stream().sorted(Comparator.comparing(DIDUpdatePayload::targetVersionId)).toList();

        for (DIDUpdatePayload didUpdatePayload : orderedDidUpdatePayloads) {
            if (didUpdatePayload.targetVersionId() <= currentVersionId) {
                confirmDuplicateUpdate(didUpdatePayload, updateHashHistory, contemporaryHash);
            } else if (didUpdatePayload.targetVersionId() == currentVersionId + 1) {
                if (! Arrays.equals(didUpdatePayload.sourceHash(), contemporaryHash)) {
                    throw new ResolutionException("latePublishing", "update.sourceHash " + HexUtil.hexEncode(didUpdatePayload.sourceHash()) + " does not match contemporaryHash: " + HexUtil.hexEncode(contemporaryHash));
                }
                contemporaryDIDDocument = applyDIDUpdate(contemporaryDIDDocument, didUpdatePayload);
                currentVersionId++;
                if (currentVersionId.equals(targetVersionId)) {
                    return contemporaryDIDDocument;
                }
                byte[] updateHash = JsonCanonicalizationAndHash.jsonCanonicalizationAndHash(didUpdatePayload);
                updateHashHistory.add(updateHash);
                contemporaryHash = JsonCanonicalizationAndHash.jsonCanonicalizationAndHash(contemporaryDIDDocument);
            } else if (didUpdatePayload.targetVersionId() > currentVersionId + 1) {
                throw new ResolutionException("latePublishing", "update.targetVersionId " + didUpdatePayload.targetVersionId() + " is greater than currentVersionId + 1: " + (currentVersionId + 1));
            }
        }

        if (Objects.equals(contemporaryBlockheight, targetBlockheight)) {
            if (log.isDebugEnabled()) log.debug("traverseBlockchainHistory: " + contemporaryDIDDocument);
            return contemporaryDIDDocument;
        }

        contemporaryBlockheight++;

        DIDDocument targetDocument = this.traverseBlockchainHistory(contemporaryDIDDocument, contemporaryBlockheight, currentVersionId, targetVersionId, targetBlockheight, updateHashHistory, signalsMetadata, network, didDocumentMetadata);

        if (log.isDebugEnabled()) log.debug("traverseBlockchainHistory: " + targetDocument);
        return targetDocument;
    }

    private static final String COINBASE_TX_IDENTIFIER = "0000000000000000000000000000000000000000000000000000000000000000";
    private static final String GENESIS_TX_IDENTIFIER = "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b";

    // See https://dcdpr.github.io/did-btc1/#find-next-signals
    private NextSignals findNextSignals(Integer contemporaryBlockheight, Integer targetBlockheight, List<Beacon> beacons, Network network, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) {
        if (log.isDebugEnabled()) log.debug("findNextSignals ({}, {}, {}, {})", contemporaryBlockheight, targetBlockheight, beacons, network);

        List<Signal> signals = new ArrayList<>();

        Block block = this.getBitcoinConnection().getBlockByBlockHeight(network, contemporaryBlockheight);

        block.txs().stream().map(Tx::txId).forEach(txId -> {
            if (COINBASE_TX_IDENTIFIER.equals(txId)) return;
            if (GENESIS_TX_IDENTIFIER.equals(txId)) return;
            Tx tx = this.getBitcoinConnection().getTransactionById(network, txId);
            for (TxIn txIn : tx.txIns()) {
                String prevTxId = txIn.txId();
                if (prevTxId == null) continue;
                if (COINBASE_TX_IDENTIFIER.equals(prevTxId)) continue;
                if (GENESIS_TX_IDENTIFIER.equals(prevTxId)) continue;
                Tx prevTx = this.getBitcoinConnection().getTransactionById(network, prevTxId);
                TxOut spentTxOut = prevTx.txOuts().get(txIn.transactionOutputN());
                Address spentAddress = spentTxOut.scriptPubKeyAddress() == null ? null : AddressParser.getDefault(network.toBitcoinjNetwork()).parseAddress(spentTxOut.scriptPubKeyAddress());
                Beacon foundBeacon = beacons.stream().filter(beacon -> beacon.address().equals(spentAddress)).findAny().orElse(null);
                if (foundBeacon != null) {
                    Signal beaconSignal = new Signal(foundBeacon.id(), foundBeacon.type(), tx);
                    signals.add(beaconSignal);
                    break;
                }
            }
        });

        boolean recursive;
        NextSignals nextSignals;

        if (Objects.equals(contemporaryBlockheight, targetBlockheight)) {
            recursive = false;
            nextSignals = new NextSignals(contemporaryBlockheight, signals);
        } else if (signals.isEmpty()) {
            recursive = true;
            nextSignals = this.findNextSignals(contemporaryBlockheight + 1, targetBlockheight, beacons, network, didDocumentMetadata);
        } else {
            recursive = false;
            nextSignals = new NextSignals(contemporaryBlockheight, signals);
        }

        // DID DOCUMENT METADATA

        if (! recursive) {
            Map<Integer, Map<String, Object>> didDocumentMetadataNextSignals = (Map<Integer, Map<String, Object>>) didDocumentMetadata.computeIfAbsent("nextSignals", x -> new LinkedHashMap<>());
            if (! nextSignals.signals().isEmpty()) didDocumentMetadataNextSignals.put(contemporaryBlockheight, RecordUtil.toMap(nextSignals));
        }

        // done

        if (log.isDebugEnabled()) log.debug("findNextSignals: " + nextSignals);
        return nextSignals;
    }

    // See https://dcdpr.github.io/did-btc1/#process-beacon-signals
    private List<DIDUpdatePayload> processBeaconSignals(List<Signal> beaconSignals, Map<String, Object> signalsMetadata, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) throws ResolutionException {
        if (log.isDebugEnabled()) log.debug("processBeaconSignals ({}, {})", beaconSignals, signalsMetadata);

        List<DIDUpdatePayload> updates = new ArrayList<>();

        for (Signal beaconSignal : beaconSignals) {

            String type = beaconSignal.beaconType();
            Tx signalTx = beaconSignal.tx();
            String signalId = signalTx.txId();
            Map<String, Object> signalSidecarData = (Map<String, Object>) signalsMetadata.get(signalId);

            DIDUpdatePayload didUpdatePayload = switch(type) {
                case SingletonBeacon.TYPE -> SingletonBeacon.processSingletonBeaconSignal(signalTx, signalSidecarData, this.getIpfsConnection(), didDocumentMetadata);
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
    private static void confirmDuplicateUpdate(DIDUpdatePayload update, List<byte[]> updateHashHistory, byte[] contemporaryHash) throws ResolutionException {
        if (log.isDebugEnabled()) log.debug("confirmDuplicateUpdate ({}, {}, {})", update, updateHashHistory, contemporaryHash);

        byte[] updateHash = JsonCanonicalizationAndHash.jsonCanonicalizationAndHash(update);
        Integer updateHashIndex = update.targetVersionId() - 2;
        byte[] historicalUpdateHash = updateHashHistory.get(updateHashIndex);
        if (! Arrays.equals(historicalUpdateHash, updateHash)) {
            throw new ResolutionException("latePublishing", "historicalUpdateHash " + HexUtil.hexEncode(historicalUpdateHash) + " does not match updateHash: " + HexUtil.hexEncode(updateHash));
        }
    }

    // See https://dcdpr.github.io/did-btc1/#apply-did-update
    private static DIDDocument applyDIDUpdate(DIDDocument contemporaryDIDDocument, DIDUpdatePayload update) throws ResolutionException {
        if (log.isDebugEnabled()) log.debug("applyDIDUpdate ({}, {})", contemporaryDIDDocument, update);

/* TODO       DataIntegrityProof dataIntegrityProof = DataIntegrityProof.builder()
                .type(DataIntegritySuites.DATA_INTEGRITY_SUITE_DATAINTEGRITYPROOF.getTerm())
                .cryptosuite("bip340-rdfc-2025")*/

        DIDDocument targetDIDDocument = DIDDocumentUtil.copy(contemporaryDIDDocument);
        targetDIDDocument = JSONPatchUtil.apply(targetDIDDocument, update.patch());
        Validation.validate(targetDIDDocument);
        byte[] targetHash = JsonCanonicalizationAndHash.jsonCanonicalizationAndHash(targetDIDDocument);
        if (! Arrays.equals(targetHash, update.targetHash())) {
            throw new ResolutionException("invalidDidUpdate", "targetHash " + HexUtil.hexEncode(targetHash) + " does not match update.targetHash: " + HexUtil.hexEncode(update.targetHash()));
        }

        if (log.isDebugEnabled()) log.debug("applyDIDUpdate: " + targetDIDDocument);
        return targetDIDDocument;
    }

    /*
     * Getters and setters
     */

    public BitcoinConnection getBitcoinConnection() {
        return this.bitcoinConnection;
    }

    public void setBitcoinConnection(BitcoinConnection bitcoinConnection) {
        this.bitcoinConnection = bitcoinConnection;
    }

    public IPFSConnection getIpfsConnection() {
        return this.ipfsConnection;
    }

    public void setIpfsConnection(IPFSConnection ipfsConnection) {
        this.ipfsConnection = ipfsConnection;
    }
}
