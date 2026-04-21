# Universal Resolver Driver: did:btcr2

This is a [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/) driver for **did:btcr2** identifiers.

## Specifications

* [Decentralized Identifiers](https://www.w3.org/TR/did-1.0/)
* [DID Method Specification](https://dcdpr.github.io/did-btcr2/)

## Build and Run (Docker)

```
docker compose build
docker compose up
```

## Example Requests

Public Key-based did:btcr2 on Mutinynet without Update:

```
curl -X GET 'http://localhost:8080/1.0/identifiers/did:btcr2:k1q5ppmnfjqp0qe5klmnll9tazz9jd5ds43x5xfsr3hu9jdgaldu0d3jgs0vj4r'
```

Public Key-based did:btcr2 on Mutinynet with 1 Update (from Sidecar):

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:k1q5p7drc8y5hhmvs2nncyuq73ts98arnqv5ce446vwydafuu2mp9rp6szethjk' -d '{"sidecar":{"updates":[{"@context":["https://btcr2.dev/context/v1","https://w3id.org/json-ld-patch/v1","https://w3id.org/zcap/v1","https://w3id.org/security/data-integrity/v2"],"patch":[{"op":"add","path":"/service/3","value":{"id":"#didcomm","type":"DIDCommMessaging","serviceEndpoint":"http://example.com/didcomm/"}}],"sourceHash":"osaUrsl3XhLlm-J4hKrqmxP6G0y9sfqseaF4HPPUe_8","targetHash":"eS9wA7p1qiQHGEuyz4swjnZNnFz9VkBSPff6i6XeP_M","targetVersionId":2,"proof":{"type":"DataIntegrityProof","cryptosuite":"bip340-jcs-2025","verificationMethod":"did:btcr2:k1q5p7drc8y5hhmvs2nncyuq73ts98arnqv5ce446vwydafuu2mp9rp6szethjk#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ak1q5p7drc8y5hhmvs2nncyuq73ts98arnqv5ce446vwydafuu2mp9rp6szethjk","capabilityAction":"Write","proofValue":"z5DhhjMk8gXULWNKRwqJqGbqZF94ov7zjKS6YkaCGxoDa2f3WT3zprMUc1p62w2cRRdkNAhR4rfGJpxZZFfHsdrJB"}}]}}'
```

Genesis document-based did:btcr2 on Mutinynet without Update:

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1q48psqn0s2fd03nu35ccwsfur6fhx7qwa42uxtf4gch592w45lkdumq73zt' -d '{"sidecar":{"genesisDocument":{"verificationMethod":[{"type":"Multikey","id":"#initialKey","publicKeyMultibase":"zQ3shjzRm1832EuqTXGQ8EyPuGePQCjUvRPekwxWFaX5qg5oG"}],"service":[{"id":"#didcomm","type":"DIDCommMessaging","serviceEndpoint":"http://localhost/"}],"assertionMethod":["#initialKey"],"capabilityDelegation":["#initialKey"],"capabilityInvocation":["#initialKey"],"@context":["https://www.w3.org/ns/did/v1.1","https://btcr2.dev/context/v1"],"authentication":["#initialKey"],"id":"did:btcr2:_"}}}'
```

Genesis document-based did:btcr2 on Mutinynet with 1 Update (from Sidecar):

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1q5d4fqz7fp6e7dlyc9yeeepytzdqhegs8quzjfj3zyn5nx62zqsfsrf77dk' -d '{"sidecar":{"genesisDocument":{"verificationMethod":[{"type":"Multikey","id":"#initialKey","publicKeyMultibase":"zQ3shukJPNjyebn1ougtYiYmBeBvSV79WJ7SJwGU1PdNvpfoT"}],"service":[{"id":"#didcomm","type":"DIDCommMessaging","serviceEndpoint":"https://test.example.com/didcomm"},{"type":"SingletonBeacon","id":"#initialP2PKH","serviceEndpoint":"bitcoin:mkDXg7GBwyJsBxVFznjnuZSHCzBNymaMHM"},{"type":"SingletonBeacon","id":"#initialP2WPKH","serviceEndpoint":"bitcoin:tb1qxwxsrz75yttwnvtrq63d60m0wtwtylv7pdlnaw"},{"type":"SingletonBeacon","id":"#initialP2TR","serviceEndpoint":"bitcoin:tb1penz0flulv59cnp96nw60a4wmw56nkj877t8vc26fwjstyjtv7z8qr6h46l"}],"assertionMethod":["#initialKey"],"capabilityDelegation":["#initialKey"],"capabilityInvocation":["#initialKey"],"@context":["https://www.w3.org/ns/did/v1.1","https://btcr2.dev/context/v1"],"authentication":["#initialKey"],"id":"did:btcr2:_"},"updates":[{"@context":["https://btcr2.dev/context/v1","https://w3id.org/json-ld-patch/v1","https://w3id.org/zcap/v1","https://w3id.org/security/data-integrity/v2"],"patch":[{"op":"add","path":"/service/4","value":{"id":"#dwn","type":"DecentralizedWebNode","serviceEndpoint":"http://my.service.com/dwn"}}],"sourceHash":"XQkK_8IWFKTQIPNW_9G-f1KhgpYCK0jbkISZ53JBJpg","targetHash":"UhsASA4PPlthQUkCbsAv_dFQBtPhrZ4binAJwfmqQIc","targetVersionId":2,"proof":{"type":"DataIntegrityProof","cryptosuite":"bip340-jcs-2025","verificationMethod":"#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ax1q5d4fqz7fp6e7dlyc9yeeepytzdqhegs8quzjfj3zyn5nx62zqsfsrf77dk","capabilityAction":"Write","proofValue":"z4PCo1d9mwp1ND9V1e36kJFdzbpzYGYTqU2KAS2ZTWkb7284wz5A9zXqzmzuEr2HLH29xsHLzFny5bpMzj85cFMtL"}}]}}'
```

## Driver Environment Variables

The driver recognizes the following environment variables:

### `uniresolver_driver_did_btcr2_bitcoinConnections`

 * Specifies how the driver interacts with the Bitcoin blockchain.
 * Possible values: 
   * `bitcoind`: Connects to a [bitcoind](https://bitcoin.org/en/full-node) instance via JSON-RPC
   * `btcd`: Connects to a [btcd](https://github.com/btcsuite/btcd) instance via JSON-RPC
   * `bitcoinj`: Connects to Bitcoin using a local [bitcoinj](https://bitcoinj.github.io/) client
   * `blockcypherapi`: Connects to [BlockCypher's API](https://www.blockcypher.com/dev/bitcoin/)
   * `esploraelectrsrest`: Connects to Esplora/Electrs REST API
 * Default value: `bitcoind`

### `uniresolver_driver_did_btcr2_bitcoinConnectionsUrls`

 * Specifies the JSON-RPC URLs of the Bitcoin connections.

### `uniresolver_driver_did_btcr2_bitcoinConnectionsCerts`

 * Specifies the server TLS certificates of the Bitcoin connections.
 * Default value: ``

### `uniresolver_driver_did_btcr2_genesisHashes`

* Specifies genesis hashes associated with the Bitcoin connections.
* Default value: ``

### `uniresolver_driver_did_btcr2_ipfs`

* Specifies a MultiAddress of an IPFS connection to be used as CAS.
* Default value: `/ip4/127.0.0.1/tcp/5001`

## Driver Metadata

The driver returns the following metadata in addition to a DID document:

* `blockHeight`: ...
* `blockIndex`: ...
