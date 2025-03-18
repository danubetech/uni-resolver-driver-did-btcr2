package uniresolver.driver.did.btc1;

import com.apicatalog.multicodec.codec.KeyCodec;
import com.danubetech.dataintegrity.jsonld.DataIntegrityContexts;
import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.Service;
import foundation.identity.did.VerificationMethod;
import io.ipfs.multibase.Multibase;
import org.bitcoinj.base.Address;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.uri.BitcoinURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class Resolver {

    private static final Logger log = LoggerFactory.getLogger(Resolver.class);

    // See https://dcdpr.github.io/did-btc1/#resolve-initial-document
    public static DIDDocument resolveInitialDIDDocument(DID identifier, IdentifierComponents identifierComponents, Map<String, Object> resolveOptions) throws ResolutionException {

        DIDDocument didDocument;

        if ("k".equals(identifierComponents.getHrp())) {
            didDocument = deterministicallyGenerateInitialDIDDocument(identifier, identifierComponents);
        } else if ("x".equals(identifierComponents.getHrp())) {
            didDocument = externalResolution(identifier, identifierComponents, resolveOptions);
        } else {
            throw new ResolutionException("invalidHRPValue", "Invalid HRP value: " + identifierComponents.getHrp());
        }

        if (log.isDebugEnabled()) log.debug("resolveInitialDIDDocument: " + didDocument);
        return didDocument;
    }

    // See https://dcdpr.github.io/did-btc1/#deterministically-generate-initial-did-document
    private static DIDDocument deterministicallyGenerateInitialDIDDocument(DID identifier, IdentifierComponents identifierComponents) throws ResolutionException {

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

        List<Service> services = deterministicallyGenerateBeaconServices(keyBytes, identifierComponents.getNetwork());

        didDocumentBuilder.services(services);

        DIDDocument didDocument = didDocumentBuilder.build();
        if (log.isDebugEnabled()) log.debug("deterministicallyGenerateInitialDIDDocument: " + didDocument);
        return didDocument;
    }

    // See https://dcdpr.github.io/did-btc1/#deterministically-generate-beacon-services
    private static List<Service> deterministicallyGenerateBeaconServices(byte[] keyBytes, Network network) {

        ScriptBuilder.createP2PKHOutputScript(ECKey.fromPublicOnly(keyBytes)).getToAddress(network.toBitcoinjNetwork());

        String initialP2PKHBeaconAddress = null;
        String initialP2WPKHBeaconAddress = null;
        String initialP2TRBeaconAddress = null;

        List<Service> services = List.of(
            establishSingletonBeacon(URI.create("#initialP2PKH"), initialP2PKHBeaconAddress, network),
            establishSingletonBeacon(URI.create("#initialP2WPKH"), initialP2WPKHBeaconAddress, network),
            establishSingletonBeacon(URI.create("#initialP2TR"), initialP2TRBeaconAddress, network)
        );

        if (log.isDebugEnabled()) log.debug("deterministicallyGenerateBeaconServices: " + services);
        return services;
    }

    // See https://dcdpr.github.io/did-btc1/#establish-singleton-beacon
    private static Service establishSingletonBeacon(URI serviceId, String beaconAddress, Network network) {

        URI bip21ServiceEndpoint = URI.create(BitcoinURI.convertToBitcoinURI(network.toBitcoinjNetwork(), beaconAddress, null, null, null));

        Service.Builder<? extends Service.Builder<?>> serviceBuilder = Service.builder();
        serviceBuilder.id(serviceId);
        serviceBuilder.type("SingletonBeacon");
        serviceBuilder.serviceEndpoint(bip21ServiceEndpoint);

        Service service = serviceBuilder.build();
        if (log.isDebugEnabled()) log.debug("establishSingletonBeacon: " + service);
        return service;
    }

    private static DIDDocument externalResolution(DID identifier, IdentifierComponents identifierComponents, Map<String, Object> resolveOptions) throws ResolutionException {

        // TODO

        return DIDDocument.builder().build();
    }
}
