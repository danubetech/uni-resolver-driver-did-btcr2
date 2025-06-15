package uniresolver.driver.did.btc1.connections.bitcoin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btc1.connections.bitcoin.records.Block;
import uniresolver.driver.did.btc1.connections.bitcoin.records.Tx;
import uniresolver.driver.did.btc1.connections.bitcoin.records.TxIn;
import uniresolver.driver.did.btc1.connections.bitcoin.records.TxOut;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class EsploraElectrsRESTBitcoinConnection extends AbstractBitcoinConnection implements BitcoinConnection {

	private static final Logger log = LoggerFactory.getLogger(EsploraElectrsRESTBitcoinConnection.class);

	private static final ObjectMapper objectMapper;

	private final URI apiEndpointBase;

	static {
		objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private EsploraElectrsRESTBitcoinConnection(URI apiEndpointBase) {
		if (log.isDebugEnabled()) log.debug("Creating EsploraElectrsRESTBitcoinConnection: " + apiEndpointBase);
		this.apiEndpointBase = apiEndpointBase;
	}

	public static EsploraElectrsRESTBitcoinConnection create(URI apiEndpointBase) {
		if (log.isDebugEnabled()) log.debug("Creating EsploraElectrsRESTBitcoinConnection: " + apiEndpointBase);
		return new EsploraElectrsRESTBitcoinConnection(apiEndpointBase);
	}

	@Override
	public Block getBlockByBlockHeight(Integer blockHeight) {
		URI apiEndpoint1 = URI.create(this.apiEndpointBase + "/block-height/" + blockHeight);
		Map<String, Object> response1 = readObject(apiEndpoint1);
		URI apiEndpoint2 = URI.create(this.apiEndpointBase + "/block/" + response1.get("height") + "/txs");
		List<Object> response2 = readArray(apiEndpoint2);
		Integer responseBlockHeight = ((Number) response1.get("height")).intValue();
		String responseHash = (String) response1.get("id");
		throw new RuntimeException("test");
		//List<Tx> txs = response2.stream().map()
//		Block block = new Block();
//		block.
	}

	@Override
	public Tx getTransactionById(String txid) {
		URI apiEndpoint = URI.create(this.apiEndpointBase + "/tx/" + txid);
		throw new RuntimeException("test4");
	}

	@Override
	public Block getBlockByTargetTime(Long targetTime) {
		URI apiEndpoint = URI.create(this.apiEndpointBase + "/v1/mining/blocks/timestamp/" + targetTime);
		throw new RuntimeException("test3");
	}

	@Override
	public Block getBlockByMinConfirmations(Integer confirmations) {
		throw new RuntimeException("Not implemented yet");
	}

	/*
	 * Helper methods
	 */

	public Tx txFromObject(Map<String, Object> object) {
		String txId = (String) object.get("txid");
		if (Tx.COINBASE_TX_IDENTIFIER.equals(txId) || Tx.GENESIS_TX_IDENTIFIER.equals(txId)) {
			return new Tx(txId, null, null, null);
		}
		URI apiEndpoint = URI.create(this.apiEndpointBase + "/tx/" + txId + "/hex");
		String txHex = readString(apiEndpoint);
		throw new RuntimeException("test2");
/*		List< >
		List<TxIn> txIns = bitcoinjRawTransaction.vIn().stream().map(TxIn::fromBitcoinjIn).toList();
		List<TxOut> txOuts = bitcoinjRawTransaction.vOut().stream().map(TxOut::fromBitcoinjOut).toList();
		return new Tx(txId, txHex, txIns, txOuts);*/
	}

	private static String readString(URI uri) {
		HttpURLConnection connection;
		StringBuilder buffer = new StringBuilder();
		try {
			connection = (HttpURLConnection) uri.toURL().openConnection();
			int httpStatus = connection.getResponseCode();
			if (httpStatus != HttpURLConnection.HTTP_OK) throw new IOException("Unexpected HTTP status: " + httpStatus);
			try (InputStream inputStream = connection.getInputStream()) {
				if (inputStream == null) throw new IOException("No input stream");
				if (connection.getInputStream() != null) {
					BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String inputLine;
					while ((inputLine = in.readLine()) != null) buffer.append(inputLine);
					in.close();
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException("Cannot read from " + uri + "; " + ex.getMessage(), ex);
		}
		return buffer.toString();
    }

	private static Map<String, Object> readObject(URI uri) {
		try {
			return (Map<String, Object>) objectMapper.readValue(readString(uri), Map.class);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException("Cannot parse object response from " + uri + "; " + ex.getMessage(), ex);
		}
	}

	private static List<Object> readArray(URI uri) {
		try {
			return (List<Object>) objectMapper.readValue(readString(uri), Map.class);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException("Cannot parse array response from " + uri + "; " + ex.getMessage(), ex);
		}
	}

	/*
	 * Getters and setters
	 */

	public URI getApiEndpointBase() {
		return apiEndpointBase;
	}
}
