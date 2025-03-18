package uniresolver.driver.did.btc1;

import foundation.identity.did.*;
import info.weboftrust.btctxlookup.Chain;
import info.weboftrust.btctxlookup.ChainAndLocationData;
import info.weboftrust.btctxlookup.ChainAndTxid;
import info.weboftrust.btctxlookup.DidBtcrData;
import info.weboftrust.btctxlookup.bitcoinconnection.BitcoinConnection;
import io.leonard.Base58;
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

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bitcoinj.core.Utils.HEX;

public class DidBtc1Driver implements Driver {

	private static final Logger log = LoggerFactory.getLogger(DidBtc1Driver.class);

	private Map<String, Object> properties;

	private BitcoinConnection bitcoinConnectionMainnet;
	private BitcoinConnection bitcoinConnectionTestnet;
	private HttpClient httpClient = HttpClients.createDefault();

	public DidBtc1Driver() {
		this(Configuration.getPropertiesFromEnvironment());
	}

	public DidBtc1Driver(Map<String, Object> properties) {
		this.setProperties(properties);
	}

	public BitcoinConnection getBitcoinConnectionMainnet() {
		return this.bitcoinConnectionMainnet;
	}

	public BitcoinConnection getBitcoinConnectionTestnet() {
		return this.bitcoinConnectionTestnet;
	}

	@Override
	public ResolveResult resolve(DID identifier, Map<String, Object> resolveOptions) throws ResolutionException {

		// parse identifier

		IdentifierComponents identifierComponents = IdentifierComponents.parse(identifier);
		if (log.isDebugEnabled()) log.debug("Parsed identifier: " + identifierComponents);

		// retrieve BTC1 data

		ChainAndLocationData initialChainAndLocationData;
		ChainAndLocationData chainAndLocationData;
		ChainAndTxid initialChainAndTxid;
		ChainAndTxid chainAndTxid;
		DidBtcrData btc1Data;
		List<DidBtcrData> spentInChainAndTxids = new ArrayList<>();

		try {

			// decode txref

			chainAndLocationData = ChainAndLocationData.txrefDecode(methodSpecificIdentifier);

			if (chainAndLocationData.getLocationData().getTxoIndex() == 0 && chainAndLocationData.isExtended()) {

				String correctTxref = ChainAndLocationData.txrefEncode(chainAndLocationData);
				String correctDid = "did:btc1:" + correctTxref.substring(correctTxref.indexOf(":") + 1);
				throw new ResolutionException("Extended txref form not allowed if txoIndex == 0. You probably want to use " + correctDid +  " instead.");
			}

			// lookup txid

			BitcoinConnection connection = chainAndLocationData.getChain() == Chain.MAINNET
			                               ? this.bitcoinConnectionMainnet
			                               : this.bitcoinConnectionTestnet;

			if (connection == null) {
				throw new ResolutionException("No connection is available for the chain " + chainAndLocationData.getChain().toString());
			}

			chainAndTxid = connection.lookupChainAndTxid(chainAndLocationData);

			// loop

			initialChainAndTxid = chainAndTxid;
			initialChainAndLocationData = chainAndLocationData;

			while (true) {

				btc1Data = connection.getDidBtcrData(chainAndTxid);
				if (btc1Data == null) throw new ResolutionException("No did:btc1 data found in transaction: " + chainAndTxid);

				// check if we need to follow the tip

				if (btc1Data.getSpentInChainAndTxid() == null) {
					break;
				} else {

					spentInChainAndTxids.add(btc1Data);
					chainAndTxid = btc1Data.getSpentInChainAndTxid();
					chainAndLocationData = connection.lookupChainAndLocationData(chainAndTxid);

					// deactivated?
					if (btc1Data.isDeactivated()) {
						log.debug("DID Document is deactivated with TX: " + chainAndTxid.getTxid());
						break;
					}
				}
			}
		} catch (IOException ex) {
			throw new ResolutionException("Cannot retrieve did:btc1 data for " + methodSpecificIdentifier + ": " + ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("Retrieved did:btc1 data for " + methodSpecificIdentifier + " (" + chainAndTxid + " on chain " + chainAndLocationData.getChain() + "): " + btc1Data);

		// DID DOCUMENT verificationMethods

		List<VerificationMethod> verificationMethods = new ArrayList<>();
		List<VerificationMethod> authenticationVerificationMethods = new ArrayList<>();

		List<String> inputScriptPubKeys = new ArrayList<>();

		for (DidBtcrData spentInChainAndTxid : spentInChainAndTxids) {
			inputScriptPubKeys.add(spentInChainAndTxid.getInputScriptPubKey());
		}
		inputScriptPubKeys.add(btc1Data.getInputScriptPubKey());

		int keyNum = 0;

		for (String inputScriptPubKey : inputScriptPubKeys) {

			String keyId = did + "#key-" + (keyNum++);

			VerificationMethod verificationMethod = VerificationMethod.builder()
			                                                          .id(URI.create(keyId))
			                                                          .publicKeyBase58(Base58.encode(HEX.decode(inputScriptPubKey)))
			                                                          .build();
			verificationMethods.add(verificationMethod);
		}

		VerificationMethod verificationMethod = VerificationMethod
				.builder()
				.id(URI.create(did + "#satoshi"))
				.publicKeyBase58(Base58.encode(HEX.decode(inputScriptPubKeys.get(inputScriptPubKeys.size() - 1))))
				.build();
		verificationMethods.add(verificationMethod);

		VerificationMethod authentication = VerificationMethod.builder()
		                                              .id(URI.create("#satoshi"))
		                                              .build();
		authenticationVerificationMethods.add(authentication);

		// DID DOCUMENT services

		List<Service> services = Collections.emptyList();

		// create DID DOCUMENT

		DIDDocument didDocument = DIDDocument.builder()
		                                     .id(did.toUri())
		                                     .verificationMethods(verificationMethods)
		                                     .authenticationVerificationMethods(authenticationVerificationMethods)
		                                     .services(services)
		                                     .build();

		// create METHOD METADATA

		Map<String, Object> didDocumentMetadata = new LinkedHashMap<>();
		didDocumentMetadata.put("inputScriptPubKey", btc1Data.getInputScriptPubKey());
		didDocumentMetadata.put("continuationUri", btc1Data.getContinuationUri());
		if (chainAndLocationData != null) didDocumentMetadata.put("chain", chainAndLocationData.getChain());
		didDocumentMetadata.put("initialBlockHeight", initialChainAndLocationData.getLocationData().getBlockHeight());
		didDocumentMetadata.put("initialTransactionPosition", initialChainAndLocationData.getLocationData().getTransactionPosition());
		didDocumentMetadata.put("initialTxoIndex", initialChainAndLocationData.getLocationData().getTxoIndex());
		if (initialChainAndTxid != null) didDocumentMetadata.put("initialTxid", initialChainAndTxid);
		if (chainAndLocationData != null) didDocumentMetadata.put("blockHeight", chainAndLocationData.getLocationData().getBlockHeight());
		if (chainAndLocationData != null) didDocumentMetadata.put("transactionPosition", chainAndLocationData.getLocationData().getTransactionPosition());
		if (chainAndLocationData != null) didDocumentMetadata.put("txoIndex", chainAndLocationData.getLocationData().getTxoIndex());
		if (chainAndTxid != null) didDocumentMetadata.put("txid", chainAndTxid);
		didDocumentMetadata.put("spentInChainAndTxids", spentInChainAndTxids);
		didDocumentMetadata.put("deactivated", btc1Data.isDeactivated());

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

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
		Configuration.configureFromProperties(this, properties);
	}

	public void setBitcoinConnectionMainnet(BitcoinConnection bitcoinConnectionMainnet) {
		this.bitcoinConnectionMainnet = bitcoinConnectionMainnet;
	}

	public void setBitcoinConnectionTestnet(BitcoinConnection bitcoinConnectionTestnet) {
		this.bitcoinConnectionTestnet = bitcoinConnectionTestnet;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}
}
