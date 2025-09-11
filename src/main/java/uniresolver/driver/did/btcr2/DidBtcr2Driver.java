package uniresolver.driver.did.btcr2;

import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.DIDURL;
import foundation.identity.did.representations.Representations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.driver.did.btcr2.config.Configuration;
import uniresolver.driver.did.btcr2.crud.read.Read;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.util.LinkedHashMap;
import java.util.Map;

public class DidBtcr2Driver implements Driver {

	private static final Logger log = LoggerFactory.getLogger(DidBtcr2Driver.class);

	private Map<String, Object> properties;

	private Read read;

	public DidBtcr2Driver() {
		this(Configuration.getPropertiesFromEnvironment());
	}

	public DidBtcr2Driver(Map<String, Object> properties) {
		this.setProperties(properties);
	}

	@Override
	public ResolveResult resolve(DID identifier, Map<String, Object> resolutionOptions) throws ResolutionException {

		// DID RESOLUTION METADATA

		Map<String, Object> didResolutionMetadata = new LinkedHashMap<>();
		didResolutionMetadata.put("contentType", Representations.DEFAULT_MEDIA_TYPE);

		// DID DOCUMENT METADATA

		Map<String, Object> didDocumentMetadata = new LinkedHashMap<>();

		// execute read() operation

        DIDDocument didDocument = this.getRead().read(identifier, resolutionOptions, didDocumentMetadata);

		// done

		return ResolveResult.build(didResolutionMetadata, didDocument, didDocumentMetadata);
	}

	@Override
	public DereferenceResult dereference(DIDURL didurl, Map<String, Object> map) throws DereferencingException, ResolutionException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Map<String, Object> properties() {
		return this.getProperties();
	}

	/*
	 * Getters and setters
	 */

	public Map<String, Object> getProperties() {
		return this.properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
		Configuration.configureFromProperties(this, properties);
	}

	public Read getRead() {
		return this.read;
	}

	public void setRead(Read read) {
		this.read = read;
	}
}
