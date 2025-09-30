package uniresolver.driver.did.btcr2.crud.read;

import com.apicatalog.multicodec.codec.KeyCodec;
import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.Service;
import foundation.identity.did.VerificationMethod;
import fr.acinq.bitcoin.BlockHash;
import io.ipfs.cid.Cid;
import io.ipfs.multibase.Multibase;
import io.ipfs.multihash.Multihash;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.AddressParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btcr2.Network;
import uniresolver.driver.did.btcr2.appendix.JsonCanonicalizationAndHash;
import uniresolver.driver.did.btcr2.beacons.singleton.SingletonBeacon;
import uniresolver.driver.did.btcr2.connections.bitcoin.BitcoinConnector;
import uniresolver.driver.did.btcr2.connections.ipfs.IPFSConnection;
import uniresolver.driver.did.btcr2.syntax.records.IdentifierComponents;
import uniresolver.driver.did.btcr2.util.JSONUtil;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ResolveInitialDocument {

    private static final Logger log = LoggerFactory.getLogger(ResolveInitialDocument.class);

    private Read read;
    private BitcoinConnector bitcoinConnector;
    private IPFSConnection ipfsConnection;

    public ResolveInitialDocument(Read read, BitcoinConnector bitcoinConnector, IPFSConnection ipfsConnection) {
        this.read = read;
        this.bitcoinConnector = bitcoinConnector;
        this.ipfsConnection = ipfsConnection;
    }

    /*
     * 7.2.1 Resolve Initial Document
     */

    // See https://dcdpr.github.io/did-btcr2/#resolve-initial-did-document
    public DIDDocument resolveInitialDIDDocument(DID identifier, IdentifierComponents identifierComponents, Map<String, Object> resolutionOptions, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) throws ResolutionException {
        if (log.isDebugEnabled()) log.debug("resolveInitialDIDDocument ({}, {}, {})", identifier, identifierComponents, resolutionOptions);

        DIDDocument initialDocument;

        if ("key".equals(identifierComponents.idType())) {
            initialDocument = this.deterministicallyGenerateInitialDIDDocument(identifier, identifierComponents);
        } else if ("external".equals(identifierComponents.idType())) {
            initialDocument = this.externalResolution(identifier, identifierComponents, resolutionOptions, didDocumentMetadata);
        } else {
            throw new ResolutionException("invalidHRPValue", "Invalid hrp/idType value: " + identifierComponents.idType());
        }

        // DID DOCUMENT METADATA

        didDocumentMetadata.put("initialDidDocument", initialDocument);

        // Return initialDocument.

        if (log.isDebugEnabled()) log.debug("resolveInitialDIDDocument: " + initialDocument);
        return initialDocument;
    }

    // See https://dcdpr.github.io/did-btcr2/#deterministically-generate-initial-did-document
    private DIDDocument deterministicallyGenerateInitialDIDDocument(DID identifier, IdentifierComponents identifierComponents) throws ResolutionException {

        byte[] keyBytes = identifierComponents.genesisBytes();

        DIDDocument.Builder<? extends DIDDocument.Builder<?>> initialDocumentBuilder = DIDDocument.builder();
        initialDocumentBuilder.id(identifier.toUri());
        initialDocumentBuilder.defaultContexts(false);
        /* TODO is this in the spec? */ initialDocumentBuilder.contexts(List.of(URI.create("https://www.w3.org/TR/did-1.1"), URI.create("https://did-btcr2/TBD/context")));
        initialDocumentBuilder.controller(identifier.toUri());

        VerificationMethod.Builder<? extends VerificationMethod.Builder<?>> verificationMethodBuilder = VerificationMethod.builder();
        verificationMethodBuilder.id(URI.create(identifier + "#initialKey"));
        verificationMethodBuilder.type("Multikey");
        verificationMethodBuilder.controller(identifier.toUri());
        verificationMethodBuilder.publicKeyMultibase(Multibase.encode(Multibase.Base.Base58BTC, KeyCodec.SECP256K1_PUBLIC_KEY.encode(keyBytes)));

        VerificationMethod verificationMethod = verificationMethodBuilder.build();

        initialDocumentBuilder.verificationMethod(verificationMethod);
        initialDocumentBuilder.authenticationVerificationMethodReference(verificationMethod.getId());
        initialDocumentBuilder.assertionMethodVerificationMethodReference(verificationMethod.getId());
        initialDocumentBuilder.capabilityInvocationVerificationMethodReference(verificationMethod.getId());
        initialDocumentBuilder.capabilityDelegationVerificationMethodReference(verificationMethod.getId());

        List<Service> services = this.deterministicallyGenerateBeaconServices(identifier, keyBytes, identifierComponents.network());

        initialDocumentBuilder.services(services);

        DIDDocument initialDocument = initialDocumentBuilder.build();
        if (log.isDebugEnabled()) log.debug("deterministicallyGenerateInitialDIDDocument: " + initialDocument);
        return initialDocument;
    }

    // See https://dcdpr.github.io/did-btcr2/#deterministically-generate-beacon-services
    private List<Service> deterministicallyGenerateBeaconServices(DID identifier, byte[] keyBytes, Network network) {

        fr.acinq.bitcoin.PublicKey initialPublicKey = fr.acinq.bitcoin.PublicKey.parse(keyBytes);

        List<Service> services = new ArrayList<>();

        URI initialP2PKHServiceId = URI.create(identifier + "#initialP2PKH");
        Address initialP2PKHBeaconAddress = AddressParser.getDefault().parseAddress(initialPublicKey.p2pkhAddress(new BlockHash(this.getBitcoinConnector().getGensisHash(network))));
        Service p2pkhBeacon = SingletonBeacon.establishSingletonBeacon(initialP2PKHServiceId, initialP2PKHBeaconAddress, network);
        services.add(p2pkhBeacon);

        URI initialP2WPKHServiceId = URI.create(identifier + "#initialP2WPKH");
        Address initialP2WPKHBeaconAddress = AddressParser.getDefault().parseAddress(initialPublicKey.p2wpkhAddress(new BlockHash(this.getBitcoinConnector().getGensisHash(network))));
        Service p2wpkhBeacon = SingletonBeacon.establishSingletonBeacon(initialP2WPKHServiceId, initialP2WPKHBeaconAddress, network);
        services.add(p2wpkhBeacon);

        URI initialP2TRServiceId = URI.create(identifier + "#initialP2TR");
        Address initialP2TRBeaconAddress = AddressParser.getDefault().parseAddress(initialPublicKey.p2trAddress(new BlockHash(this.getBitcoinConnector().getGensisHash(network))));
        Service p2trBeacon = SingletonBeacon.establishSingletonBeacon(initialP2TRServiceId, initialP2TRBeaconAddress, network);
        services.add(p2trBeacon);

        if (log.isDebugEnabled()) log.debug("deterministicallyGenerateBeaconServices: " + services);
        return services;
    }

    // See https://dcdpr.github.io/did-btcr2/#external-resolution
    private DIDDocument externalResolution(DID identifier, IdentifierComponents identifierComponents, Map<String, Object> resolutionOptions, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) throws ResolutionException {

        DIDDocument initialDocument;

        if (resolutionOptions.get("sidecarData") instanceof Map sidecarDataMap && sidecarDataMap.get("initialDocument") instanceof Map initialDocumentMap) {
            initialDocument = this.sidecarInitialDocumentValidation(identifier, identifierComponents, initialDocumentMap, didDocumentMetadata);
        } else {
            initialDocument = this.casRetrieval(identifier, identifierComponents);
        }

        try {
            // TODO: Validation.validate(initialDocument);
        } catch (Exception ex) {
            throw new ResolutionException("invalidDidDocument", "Invalid initial DID document: " + ex.getMessage(), ex);
        }

        if (log.isDebugEnabled()) log.debug("externalResolution: " + initialDocument);
        return initialDocument;
    }

    // See https://dcdpr.github.io/did-btcr2/#sidecar-initial-document-validation
    private DIDDocument sidecarInitialDocumentValidation(DID identifier, IdentifierComponents identifierComponents, Map<String, Object> initialDocument, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) throws ResolutionException {

        // Set intermediateDocumentRepresentation to a copy of the initialDocument.

        String intermediateDocumentRepresentation = JSONUtil.mapToJson(initialDocument);

        // Find and replace all values of identifier contained within the intermediateDocumentRepresentation
        // with the string (did:btcr2:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx).

        intermediateDocumentRepresentation = intermediateDocumentRepresentation.replace(identifier.toString(), "did:btcr2:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

        // Set hashBytes to the SHA256 hash of the intermediateDocumentRepresentation.

        byte[] hashBytes = JsonCanonicalizationAndHash.jsonCanonicalizationAndHash(JSONUtil.jsonToMap(intermediateDocumentRepresentation));

        // If hashBytes does not equal identifierComponents.genesisBytes MUST throw an invalidDid error.

        if (! Arrays.equals(hashBytes, identifierComponents.genesisBytes())) {
            throw new ResolutionException(ResolutionException.ERROR_INVALID_DID, "Initial document cannot be validated");
        }

        // DID DOCUMENT METADATA

        didDocumentMetadata.put("intermediateDocumentRepresentation", intermediateDocumentRepresentation);
        didDocumentMetadata.put("intermediateDocumentRepresentationHash", Hex.encodeHexString(hashBytes));

        // Return initialDocument.

        DIDDocument initialDidDocument = DIDDocument.fromMap(initialDocument);

        if (log.isDebugEnabled()) log.debug("sidecarInitialDocumentValidation: " + initialDidDocument);
        return initialDidDocument;
    }

    // https://dcdpr.github.io/did-btcr2/#cas-retrieval
    private DIDDocument casRetrieval(DID identifier, IdentifierComponents identifierComponents) throws ResolutionException {

        byte[] hashBytes = identifierComponents.genesisBytes();
        Cid cid = Cid.buildCidV1(Cid.Codec.Raw, Multihash.Type.id, hashBytes);
        String intermediateDocumentRepresentation;
        try {
            byte[] intermediateDocumentRepresentationBytes = this.getIpfsConnection().getIpfs().get(cid.bareMultihash());
            if (intermediateDocumentRepresentationBytes == null) {
                throw new ResolutionException("Cannot find intermediate document representation for " + cid + " in CAS");
            }
            intermediateDocumentRepresentation = new String(intermediateDocumentRepresentationBytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new ResolutionException("Cannot retrieve intermediate document representation for " + cid + " from CAS: " + ex.getMessage(), ex);
        }

        String initialDocument = intermediateDocumentRepresentation.replace("did:btcr2:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", identifier.toString());
        DIDDocument parsedInitialDocument = DIDDocument.fromJson(initialDocument);

        if (log.isDebugEnabled()) log.debug("casRetrieval: " + parsedInitialDocument);
        return parsedInitialDocument;
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
