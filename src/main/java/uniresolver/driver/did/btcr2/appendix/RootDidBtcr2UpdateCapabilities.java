package uniresolver.driver.did.btcr2.appendix;

import foundation.identity.did.DID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btcr2.crud.update.jsonld.RootCapability;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RootDidBtcr2UpdateCapabilities {

    private static final Logger log = LoggerFactory.getLogger(RootDidBtcr2UpdateCapabilities.class);

    // See https://dcdpr.github.io/did-btcr2/#derive-root-capability-from-didbtcr2-identifier
    public static RootCapability deriveRootCapabilityFromDidBtcr2Identifier(DID identifier) {

        RootCapability.Builder<? extends RootCapability.Builder<?>> rootCapabilityBuilder = RootCapability.builder();

        rootCapabilityBuilder.defaultContexts(false);
        rootCapabilityBuilder.context(URI.create("https://w3id.org/zcap/v1’"));

        String encodedIdentifier = URLEncoder.encode(identifier.toString(), StandardCharsets.UTF_8);

        rootCapabilityBuilder.id(URI.create("urn:zcap:root:" + encodedIdentifier));
        rootCapabilityBuilder.controller(identifier.toUri());
        rootCapabilityBuilder.invocationTarget(identifier.toUri());

        RootCapability rootCapability = rootCapabilityBuilder.build();
        if (log.isDebugEnabled()) log.debug("deriveRootCapabilityFromDidBtcr2Identifier for " + identifier + ": " + rootCapability);
        return rootCapability;
    }

    public static RootCapability dereferenceRootCapabilityIdentifier(String capabilityId) {

        RootCapability.Builder<? extends RootCapability.Builder<?>> rootCapabilityBuilder = RootCapability.builder();

        rootCapabilityBuilder.defaultContexts(false);
        rootCapabilityBuilder.context(URI.create("https://w3id.org/zcap/v1’"));

        String[] components = capabilityId.split(":");

        if (components.length != 4) throw new IllegalArgumentException("Length of components for " + capabilityId + " is not 4: " + components.length);
        if (! "urn".equals(components[0])) throw new IllegalArgumentException("Components[0] for " + capabilityId + " is not 'urn': " + components[0]);
        if (! "zcap".equals(components[1])) throw new IllegalArgumentException("Components[1] for " + capabilityId + " is not 'zcap': " + components[1]);
        if (! "root".equals(components[2])) throw new IllegalArgumentException("Components[2] for " + capabilityId + " is not 'root': " + components[2]);

        String uriEncodedId = components[3];

        String btcr2Identifier = URLDecoder.decode(uriEncodedId, StandardCharsets.UTF_8);

        rootCapabilityBuilder.id(URI.create(capabilityId));
        rootCapabilityBuilder.controller(URI.create(btcr2Identifier));
        rootCapabilityBuilder.invocationTarget(URI.create(btcr2Identifier));

        RootCapability rootCapability = rootCapabilityBuilder.build();
        if (log.isDebugEnabled()) log.debug("dereferenceRootCapabilityIdentifier for " + capabilityId + ": " + rootCapability);
        return rootCapability;
    }
}
