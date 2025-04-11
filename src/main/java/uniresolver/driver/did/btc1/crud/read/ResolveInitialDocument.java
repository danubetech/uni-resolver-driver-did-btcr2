package uniresolver.driver.did.btc1.crud.read;

import com.apicatalog.multicodec.codec.KeyCodec;
import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.Service;
import foundation.identity.did.VerificationMethod;
import foundation.identity.did.validation.Validation;
import io.ipfs.cid.Cid;
import io.ipfs.multibase.Multibase;
import io.ipfs.multihash.Multihash;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.AddressParser;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.crypto.ECKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.beacons.singleton.SingletonBeacon;
import uniresolver.driver.did.btc1.connections.bitcoin.BitcoinConnection;
import uniresolver.driver.did.btc1.connections.ipfs.IPFSConnection;
import uniresolver.driver.did.btc1.syntax.records.IdentifierComponents;
import uniresolver.driver.did.btc1.util.SHA256Util;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ResolveInitialDocument {

    private static final Logger log = LoggerFactory.getLogger(ResolveInitialDocument.class);

    private BitcoinConnection bitcoinConnection;
    private IPFSConnection ipfsConnection;

    public ResolveInitialDocument(BitcoinConnection bitcoinConnection, IPFSConnection ipfsConnection) {
        this.bitcoinConnection = bitcoinConnection;
        this.ipfsConnection = ipfsConnection;
    }

    /*
     * 4.2.1 Resolve Initial Document
     */

    // See https://dcdpr.github.io/did-btc1/#resolve-initial-document
    public DIDDocument resolveInitialDIDDocument(DID identifier, IdentifierComponents identifierComponents, Map<String, Object> resolutionOptions, /* TODO: extra, not in spec */ Map<String, Object> didDocumentMetadata) throws ResolutionException {
        if (log.isDebugEnabled())
            log.debug("resolveInitialDIDDocument ({}, {}, {})", identifier, identifierComponents, resolutionOptions);

        DIDDocument initialDocument;

        if ("key".equals(identifierComponents.idType())) {
            initialDocument = this.deterministicallyGenerateInitialDIDDocument(identifier, identifierComponents);
        } else if ("external".equals(identifierComponents.idType())) {
            initialDocument = this.externalResolution(identifier, identifierComponents, resolutionOptions);
        } else {
            throw new ResolutionException("invalidHRPValue", "Invalid hrp/idType value: " + identifierComponents.idType());
        }

        // DID DOCUMENT METADATA

        didDocumentMetadata.put("initialDidDocument", initialDocument);

        // Return initialDocument.

        if (log.isDebugEnabled()) log.debug("resolveInitialDIDDocument: " + initialDocument);
        return initialDocument;
    }

    private static final URI CONTEXT = URI.create("https://did-btc1/TBD/context");

    // See https://dcdpr.github.io/did-btc1/#deterministically-generate-initial-did-document
    private DIDDocument deterministicallyGenerateInitialDIDDocument(DID identifier, IdentifierComponents identifierComponents) throws ResolutionException {

        byte[] keyBytes = identifierComponents.genesisBytes();

        DIDDocument.Builder<? extends DIDDocument.Builder<?>> initialDocumentBuilder = DIDDocument.builder();
        initialDocumentBuilder.id(identifier.toUri());
        initialDocumentBuilder.defaultContexts(true);
        initialDocumentBuilder.context(CONTEXT);

        VerificationMethod.Builder<? extends VerificationMethod.Builder<?>> verificationMethodBuilder = VerificationMethod.builder();
        verificationMethodBuilder.id(URI.create("#initialKey"));
        verificationMethodBuilder.type("Multikey");
        verificationMethodBuilder.controller(identifier.toUri());
        verificationMethodBuilder.publicKeyMultibase(Multibase.encode(Multibase.Base.Base58BTC, KeyCodec.SECP256K1_PUBLIC_KEY.encode(keyBytes)));

        VerificationMethod verificationMethod = verificationMethodBuilder.build();

        initialDocumentBuilder.verificationMethod(verificationMethod);
        initialDocumentBuilder.authenticationVerificationMethodReference(verificationMethod.getId());
        initialDocumentBuilder.assertionMethodVerificationMethodReference(verificationMethod.getId());
        initialDocumentBuilder.capabilityInvocationVerificationMethodReference(verificationMethod.getId());
        initialDocumentBuilder.capabilityDelegationVerificationMethodReference(verificationMethod.getId());

        List<Service> services = deterministicallyGenerateBeaconServices(keyBytes, identifierComponents.network());

        initialDocumentBuilder.services(services);

        DIDDocument initialDocument = initialDocumentBuilder.build();
        if (log.isDebugEnabled()) log.debug("deterministicallyGenerateInitialDIDDocument: " + initialDocument);
        return initialDocument;
    }

    // See https://dcdpr.github.io/did-btc1/#deterministically-generate-beacon-services
    private static List<Service> deterministicallyGenerateBeaconServices(byte[] keyBytes, Network network) {

        ECKey ecKey = ECKey.fromPublicOnly(keyBytes);

        List<Service> services = new ArrayList<>();

        URI initialP2PKHServiceId = URI.create("#initialP2PKH");
        Address initialP2PKHBeaconAddress = ecKey.toAddress(ScriptType.P2PKH, network.toBitcoinjNetwork());
        Service p2pkhBeacon = SingletonBeacon.establishSingletonBeacon(initialP2PKHServiceId, initialP2PKHBeaconAddress, network);
        services.add(p2pkhBeacon);

        URI initialP2WPKHServiceId = URI.create("#initialP2WPKH");
        Address initialP2WPKHBeaconAddress = ecKey.toAddress(ScriptType.P2WPKH, network.toBitcoinjNetwork());
        Service p2wpkhBeacon = SingletonBeacon.establishSingletonBeacon(initialP2WPKHServiceId, initialP2WPKHBeaconAddress, network);
        services.add(p2wpkhBeacon);

/*      TODO. P2TR not yet supported by bitcoinj
        URI initialP2TRServiceId = URI.create("#initialP2TR");
        Address initialP2TRBeaconAddress = ecKey.toAddress(ScriptType.P2TR, network.toBitcoinjNetwork());
        Service p2trBeacon = SingletonBeacon.establishSingletonBeacon(initialP2TRServiceId, initialP2TRBeaconAddress, network);
        services.add(p2trBeacon);*/

        URI initialP2TRServiceId = URI.create("#initialP2TR");
        Address initialP2TRBeaconAddress = AddressParser.getDefault().parseAddress("bcrt1p6rs5tnq94rt4uu5edc9luahlkyphk30yk8smwfzurpc8ru06vcws8ylq7l");
        Service p2trBeacon = SingletonBeacon.establishSingletonBeacon(initialP2TRServiceId, initialP2TRBeaconAddress, network);
        services.add(p2trBeacon);

        if (log.isDebugEnabled()) log.debug("deterministicallyGenerateBeaconServices: " + services);
        return services;
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
        if (!Arrays.equals(hashBytes, identifierComponents.genesisBytes())) {
            throw new ResolutionException(ResolutionException.ERROR_INVALIDDID, "Initial document cannot be validated");
        }

        DIDDocument validatedInitialDocument = DIDDocument.fromJson(initialDocument);

        if (log.isDebugEnabled()) log.debug("sidecarInitialDocumentValidation: " + validatedInitialDocument);
        return validatedInitialDocument;
    }

    // https://dcdpr.github.io/did-btc1/#cas-retrieval
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

        String initialDocument = intermediateDocumentRepresentation.replace("did:btc1:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", identifier.toString());
        DIDDocument parsedInitialDocument = DIDDocument.fromJson(initialDocument);

        if (log.isDebugEnabled()) log.debug("casRetrieval: " + parsedInitialDocument);
        return parsedInitialDocument;
    }

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
