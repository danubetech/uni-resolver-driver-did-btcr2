package uniresolver.driver.did.btcr2;

import foundation.identity.did.DID;
import foundation.identity.did.DIDURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.driver.did.btcr2.config.Configuration;
import uniresolver.driver.did.btcr2.crud.resolve.Resolve;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.util.Map;

public class DidBtcr2Driver implements Driver {

	private static final Logger log = LoggerFactory.getLogger(DidBtcr2Driver.class);

	private Map<String, Object> properties;

	private Resolve resolve;

	public DidBtcr2Driver() {
		this(Configuration.getPropertiesFromEnvironment());
	}

	public DidBtcr2Driver(Map<String, Object> properties) {
		this.setProperties(properties);
	}

	@Override
	public ResolveResult resolve(DID identifier, Map<String, Object> resolutionOptions) throws ResolutionException {

		// execute resolve() operation

        ResolveResult resolveResult = this.getResolve().resolve(identifier, resolutionOptions);

		// done

		return resolveResult;
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

	public Resolve getResolve() {
		return this.resolve;
	}

	public void setResolve(Resolve resolve) {
		this.resolve = resolve;
	}
}
