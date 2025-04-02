package uniresolver.driver.did.btc1;

import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.DIDURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.driver.did.btc1.config.Configuration;
import uniresolver.driver.did.btc1.crud.read.Read;
import uniresolver.driver.did.btc1.crud.read.records.IdentifierComponents;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.util.LinkedHashMap;
import java.util.Map;

public class DidBtc1Driver implements Driver {

	private static final Logger log = LoggerFactory.getLogger(DidBtc1Driver.class);

	private Map<String, Object> properties;

	private Read read;

	public DidBtc1Driver() {
		this(Configuration.getPropertiesFromEnvironment());
	}

	public DidBtc1Driver(Map<String, Object> properties) {
		this.setProperties(properties);
	}

	@Override
	public ResolveResult resolve(DID identifier, Map<String, Object> resolutionOptions) throws ResolutionException {

		// TODO
		IdentifierComponents identifierComponents = null;

		// resolve initial DID document

		DIDDocument didDocument = this.getRead().read(identifier, resolutionOptions);

		// create METHOD METADATA

		Map<String, Object> didDocumentMetadata = new LinkedHashMap<>();
		didDocumentMetadata.put("network", identifierComponents.network().name());
		didDocumentMetadata.put("version", identifierComponents.version());
		didDocumentMetadata.put("hrp", identifierComponents.hrp());

		// done

		return ResolveResult.build(null, didDocument, didDocumentMetadata);
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
