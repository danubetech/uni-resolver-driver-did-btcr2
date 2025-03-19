package uniresolver.driver.did.btc1;

import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.DIDURL;
import info.weboftrust.btctxlookup.bitcoinconnection.BitcoinConnection;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.driver.did.btc1.config.Configuration;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.util.LinkedHashMap;
import java.util.Map;

public class DidBtc1Driver implements Driver {

	private static final Logger log = LoggerFactory.getLogger(DidBtc1Driver.class);

	private Map<String, Object> properties;

	private Resolver resolver;
	private BitcoinConnection bitcoinConnectionMainnet;
	private BitcoinConnection bitcoinConnectionTestnet;
	private HttpClient httpClient = HttpClients.createDefault();

	public DidBtc1Driver() {
		this(Configuration.getPropertiesFromEnvironment());
	}

	public DidBtc1Driver(Map<String, Object> properties) {
		this.setProperties(properties);
	}

	@Override
	public ResolveResult resolve(DID identifier, Map<String, Object> resolveOptions) throws ResolutionException {

		// parse identifier

		IdentifierComponents identifierComponents = IdentifierComponents.parse(identifier);
		if (log.isDebugEnabled()) log.debug("Parsed identifier: " + identifierComponents);

		// resolve initial DID document

		DIDDocument didDocument = this.getResolver().resolveInitialDIDDocument(identifier, identifierComponents, resolveOptions);

		// create METHOD METADATA

		Map<String, Object> didDocumentMetadata = new LinkedHashMap<>();
		didDocumentMetadata.put("network", identifierComponents.getNetwork().name());
		didDocumentMetadata.put("version", identifierComponents.getVersion());
		didDocumentMetadata.put("hrp", identifierComponents.getHrp());

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

	public Resolver getResolver() {
		return this.resolver;
	}

	public void setResolver(Resolver resolver) {
		this.resolver = resolver;
	}

	public BitcoinConnection getBitcoinConnectionMainnet() {
		return this.bitcoinConnectionMainnet;
	}

	public void setBitcoinConnectionMainnet(BitcoinConnection bitcoinConnectionMainnet) {
		this.bitcoinConnectionMainnet = bitcoinConnectionMainnet;
	}

	public BitcoinConnection getBitcoinConnectionTestnet() {
		return this.bitcoinConnectionTestnet;
	}

	public void setBitcoinConnectionTestnet(BitcoinConnection bitcoinConnectionTestnet) {
		this.bitcoinConnectionTestnet = bitcoinConnectionTestnet;
	}

	public HttpClient getHttpClient() {
		return this.httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}
}
