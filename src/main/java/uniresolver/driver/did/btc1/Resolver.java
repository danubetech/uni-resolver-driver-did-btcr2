package uniresolver.driver.did.btc1;

import com.apicatalog.multicodec.codec.KeyCodec;
import com.danubetech.dataintegrity.jsonld.DataIntegrityContexts;
import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.Service;
import foundation.identity.did.VerificationMethod;
import foundation.identity.did.validation.Validation;
import io.ipfs.api.IPFS;
import io.ipfs.cid.Cid;
import io.ipfs.multibase.Multibase;
import io.ipfs.multihash.Multihash;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.uri.BitcoinURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btc1.util.SHA256Util;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Resolver {

    private static final Logger log = LoggerFactory.getLogger(Resolver.class);

    private IPFS ipfs;

    public Resolver(IPFS ipfs) {
        this.ipfs = ipfs;
    }

    // See https://dcdpr.github.io/did-btc1/#resolve-initial-document
    public DIDDocument resolveInitialDIDDocument(DID identifier, IdentifierComponents identifierComponents, Map<String, Object> resolutionOptions) throws ResolutionException {

        DIDDocument didDocument;

        if ("k".equals(identifierComponents.getHrp())) {
            didDocument = this.deterministicallyGenerateInitialDIDDocument(identifier, identifierComponents);
        } else if ("x".equals(identifierComponents.getHrp())) {
            didDocument = this.externalResolution(identifier, identifierComponents, resolutionOptions);
        } else {
            throw new ResolutionException("invalidHRPValue", "Invalid HRP value: " + identifierComponents.getHrp());
        }

        if (log.isDebugEnabled()) log.debug("resolveInitialDIDDocument: " + didDocument);
        return didDocument;
    }

    // See https://dcdpr.github.io/did-btc1/#deterministically-generate-initial-did-document
    private DIDDocument deterministicallyGenerateInitialDIDDocument(DID identifier, IdentifierComponents identifierComponents) throws ResolutionException {

        byte[] keyBytes = identifierComponents.getGenesisBytes();

        DIDDocument.Builder<? extends DIDDocument.Builder<?>> didDocumentBuilder = DIDDocument.builder();
        didDocumentBuilder.id(identifier.toUri());
        didDocumentBuilder.defaultContexts(true);
        didDocumentBuilder.context(DataIntegrityContexts.JSONLD_CONTEXT_W3ID_DATAINTEGRITY_V2);

        VerificationMethod.Builder<? extends VerificationMethod.Builder<?>> verificationMethodBuilder = VerificationMethod.builder();
        verificationMethodBuilder.id(URI.create("#initialKey"));
        verificationMethodBuilder.type("MultiKey");
        verificationMethodBuilder.context(identifier.toUri());
        verificationMethodBuilder.publicKeyMultibase(Multibase.encode(Multibase.Base.Base58BTC, KeyCodec.SECP256K1_PUBLIC_KEY.encode(keyBytes)));

        VerificationMethod verificationMethod = verificationMethodBuilder.build();

        didDocumentBuilder.verificationMethod(verificationMethod);
        didDocumentBuilder.authenticationVerificationMethodReference(verificationMethod.getId());
        didDocumentBuilder.assertionMethodVerificationMethodReference(verificationMethod.getId());
        didDocumentBuilder.capabilityInvocationVerificationMethodReference(verificationMethod.getId());
        didDocumentBuilder.capabilityDelegationVerificationMethodReference(verificationMethod.getId());

        List<Service> services = this.deterministicallyGenerateBeaconServices(keyBytes, identifierComponents.getNetwork());

        didDocumentBuilder.services(services);

        DIDDocument didDocument = didDocumentBuilder.build();
        if (log.isDebugEnabled()) log.debug("deterministicallyGenerateInitialDIDDocument: " + didDocument);
        return didDocument;
    }

    // See https://dcdpr.github.io/did-btc1/#deterministically-generate-beacon-services
    private List<Service> deterministicallyGenerateBeaconServices(byte[] keyBytes, Network network) {

        ECKey ecKey = ECKey.fromPublicOnly(keyBytes);

        List<Service> services = new ArrayList<>();

        URI initialP2PKHServiceId = URI.create("#initialP2PKH");
        Address initialP2PKHBeaconAddress = ecKey.toAddress(ScriptType.P2PKH, network.toBitcoinjNetwork());
        Service p2pkhBeacon = this.establishSingletonBeacon(initialP2PKHServiceId, initialP2PKHBeaconAddress, network);
        services.add(p2pkhBeacon);

        URI initialP2WPKHServiceId = URI.create("#initialP2WPKH");
        Address initialP2WPKHBeaconAddress = ecKey.toAddress(ScriptType.P2WPKH, network.toBitcoinjNetwork());
        Service p2wpkhBeacon = this.establishSingletonBeacon(initialP2WPKHServiceId, initialP2WPKHBeaconAddress, network);
        services.add(p2wpkhBeacon);

        URI initialP2TRServiceId = URI.create("#initialP2TR");
        Address initialP2TRBeaconAddress = ecKey.toAddress(ScriptType.P2TR, network.toBitcoinjNetwork());
        Service p2trBeacon = this.establishSingletonBeacon(initialP2TRServiceId, initialP2TRBeaconAddress, network);
        services.add(p2trBeacon);

        if (log.isDebugEnabled()) log.debug("deterministicallyGenerateBeaconServices: " + services);
        return services;
    }

    // See https://dcdpr.github.io/did-btc1/#establish-singleton-beacon
    private Service establishSingletonBeacon(URI serviceId, Address beaconAddress, Network network) {

        URI bip21ServiceEndpoint = URI.create(BitcoinURI.convertToBitcoinURI(network.toBitcoinjNetwork(), beaconAddress.toString(), null, null, null));

        Service.Builder<? extends Service.Builder<?>> serviceBuilder = Service.builder();
        serviceBuilder.id(serviceId);
        serviceBuilder.type("SingletonBeacon");
        serviceBuilder.serviceEndpoint(bip21ServiceEndpoint);

        Service service = serviceBuilder.build();
        if (log.isDebugEnabled()) log.debug("establishSingletonBeacon: " + service);
        return service;
    }

    // See https://dcdpr.github.io/did-btc1/#external-resolution
    private DIDDocument externalResolution(DID identifier, IdentifierComponents identifierComponents, Map<String, Object> resolutionOptions) throws ResolutionException {

        DIDDocument initialDocument;

        if (resolutionOptions.containsKey("sidecarData") && resolutionOptions.get("sidecarData") instanceof Map && ((Map<String, Object>) resolutionOptions.get("sidecarData")).containsKey("initialDocument")) {
            String sidecarInitialDocument = (String) ((Map<String, Object>) resolutionOptions.get("sidecarData")).get("initialDocument");
            initialDocument = this.sidecarInitialDocumentValidation(identifier, identifierComponents, sidecarInitialDocument);
        } else {
            initialDocument = this.casRetrieval(identifier, identifierComponents);
        }

        try {
            Validation.validate(initialDocument);
        } catch (Exception ex) {
            throw new ResolutionException("invalidDidDocument", "Invalid DID document: " + ex.getMessage(), ex);
        }

        if (log.isDebugEnabled()) log.debug("externalResolution: " + initialDocument);
        return initialDocument;
    }

    // See https://dcdpr.github.io/did-btc1/#sidecar-initial-document-validation
    private DIDDocument sidecarInitialDocumentValidation(DID identifier, IdentifierComponents identifierComponents, String initialDocument) throws ResolutionException {

        String intermediateDocumentRepresentation = initialDocument.replace(identifier.toString(), "did:btc1:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        byte[] hashBytes = SHA256Util.sha256(intermediateDocumentRepresentation.getBytes(StandardCharsets.UTF_8));
        if (! Arrays.equals(hashBytes, identifierComponents.getGenesisBytes())) {
            throw new ResolutionException(ResolutionException.ERROR_INVALIDDID, "Initial document cannot be validated");
        }

        DIDDocument validatedInitialDocument = DIDDocument.fromJson(initialDocument);

        if (log.isDebugEnabled()) log.debug("sidecarInitialDocumentValidation: " + validatedInitialDocument);
        return validatedInitialDocument;
    }

    // https://dcdpr.github.io/did-btc1/#cas-retrieval
    private DIDDocument casRetrieval(DID identifier, IdentifierComponents identifierComponents) throws ResolutionException {

        byte[] hashBytes = identifierComponents.getGenesisBytes();
        Cid cid = Cid.buildCidV1(Cid.Codec.Raw, Multihash.Type.id, hashBytes);
        String intermediateDocumentRepresentation;
        try {
            byte[] intermediateDocumentRepresentationBytes = this.getIpfs().get(cid.bareMultihash());
            if (intermediateDocumentRepresentationBytes == null) {
                throw new ResolutionException("Cannot find intermediate document representation for " + cid + " in CAS");
            }
            intermediateDocumentRepresentation = new String(intermediateDocumentRepresentationBytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new ResolutionException("Cannot retrieve intermediate document representation for " + cid + " from CAS: " + ex.getMessage(), ex);
        }

        String initialDocument = intermediateDocumentRepresentation.replace("did:btc1:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", identifier.toString());
        DIDDocument parsedInitialDocument = DIDDocument.fromJson(initialDocument);

        if (log.isDebugEnabled()) log.debug("casRetrieval: " + parsedInitialDocument);
        return parsedInitialDocument;
    }

    // See https://dcdpr.github.io/did-btc1/#resolve-target-document
    public DIDDocument resolveTargetDocument(DIDDocument initialDocument, Map<String, Object> resolutionOptions) throws ResolutionException {

        // TODO

        return null;
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
