package uniresolver.driver.did.btc1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.DIDURL;
import foundation.identity.did.representations.Representations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.driver.did.btc1.config.Configuration;
import uniresolver.driver.did.btc1.crud.read.Read;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.util.LinkedHashMap;
import java.util.Map;

public class DidBtc1Driver implements Driver {

	private static final Logger log = LoggerFactory.getLogger(DidBtc1Driver.class);

	private static String RESOLUTION_OPTIONS =
			"""
{
  "sidecarData": {
    "did": "did:btc1:regtest:k1qdh2ef3aqne63sdhq8tr7c8zv9lyl5xy4llj8uw3ejfj5xsuhcacjq98ccc",
    "signalsMetadata": {
      "5f8cd13f39fa509b1cdfdc7c6588b6cda99e82202e9498dff9f37dc99d4a1e10": {
        "updatePayload": {
          "@context": [
            "https://w3id.org/security/v2",
            "https://w3id.org/zcap/v1",
            "https://w3id.org/json-ld-patch/v1"
          ],
          "patch": [
            {
              "op": "add",
              "path": "/service/3",
              "value": {
                "id": "#linked-domain",
                "type": "LinkedDomains",
                "serviceEndpoint": "https://contact-me.com"
              }
            }
          ],
          "sourceHash": "9kSA9j3z2X3a26yAdJi6nwg31qyfaHMCU1u81ZrkHirM",
          "targetHash": "C45TsdfkLZh5zL6pFfRmK93X4EdHusbCDwvt8d7Xs3dP",
          "targetVersionId": 2,
          "proof": {
            "type": "DataIntegrityProof",
            "cryptosuite": "bip340-jcs-2025",
            "verificationMethod": "did:btc1:regtest:k1qdh2ef3aqne63sdhq8tr7c8zv9lyl5xy4llj8uw3ejfj5xsuhcacjq98ccc#initialKey",
            "proofPurpose": "capabilityInvocation",
            "capability": "urn:zcap:root:did%3Abtc1%3Aregtest%3Ak1qdh2ef3aqne63sdhq8tr7c8zv9lyl5xy4llj8uw3ejfj5xsuhcacjq98ccc",
            "capabilityAction": "Write",
            "@context": [
              "https://w3id.org/security/v2",
              "https://w3id.org/zcap/v1",
              "https://w3id.org/json-ld-patch/v1"
            ],
            "proofValue": "z3yfzVGdoDF4s8y4Bk8JeV9XuZw1nMeMtNW3x5brEm7DNtmWZkNBPbCLzUBJRpctBj9QJL1dydm94ZNsPxosPnkPP"
          }
        }
      }
    }
  }
}
			""";

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

		// DID RESOLUTION METADATA

		Map<String, Object> didResolutionMetadata = new LinkedHashMap<>();
		didResolutionMetadata.put("contentType", Representations.DEFAULT_MEDIA_TYPE);

		// DID DOCUMENT METADATA

		Map<String, Object> didDocumentMetadata = new LinkedHashMap<>();

		// execute read() operation

        try {
            resolutionOptions = new ObjectMapper().readValue(RESOLUTION_OPTIONS, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

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
