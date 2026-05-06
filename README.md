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

Genesis document-based (from Sidecar) did:btcr2 on Mutinynet without Update:

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1qhcxze0km2e883qex8vr45u8d4v04lmmx2zkjm3mvmtj8a9wtnny5cc2l2q' -d '{"sidecar":{"genesisDocument":{"verificationMethod":[{"type":"Multikey","id":"#initialKey","publicKeyMultibase":"zQ3shqQAyNAMywkPs8xs3boi3GgzaDs7YV5PcK67faeKn7fJJ","controller":"did:btcr2:_"}],"service":[{"id":"#didcomm","type":"DIDCommMessaging","serviceEndpoint":"http://example.com/didcomm"}],"assertionMethod":["#initialKey"],"capabilityDelegation":["#initialKey"],"capabilityInvocation":["#initialKey"],"authentication":["#initialKey"],"id":"did:btcr2:_","@context":["https://www.w3.org/ns/did/v1.1","https://btcr2.dev/context/v1"]}}}'
```

Genesis document-based (from Sidecar) did:btcr2 on Mutinynet with 1 Update (from Sidecar):

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1qkmymt685xkmzempysshq7ph56x5v9w6973nekgvmsjh3nn05eghylfh0my' -d '{"sidecar":{"genesisDocument":{"verificationMethod":[{"type":"Multikey","id":"#initialKey","publicKeyMultibase":"zQ3shiNqKWZZDM1EBzTXuhvjf3rPad4aqqpFXYry2qoRU5fgS","controller":"did:btcr2:_"}],"service":[{"id":"#didcomm","type":"DIDCommMessaging","serviceEndpoint":"http://example.com/didcomm"},{"type":"SingletonBeacon","id":"#initialP2PKH","serviceEndpoint":"bitcoin:moLcg2jmH8N9tgrSRvFK2fn3vLbdE7YHbk"},{"type":"SingletonBeacon","id":"#initialP2WPKH","serviceEndpoint":"bitcoin:tb1q2hx80sfrptyft95nhsvxj6g7ckh4avc4859p0u"},{"type":"SingletonBeacon","id":"#initialP2TR","serviceEndpoint":"bitcoin:tb1pwzyr7x97pf29v7ng9nvuk46x3ptypk3alcal2aeejq6alwslzagq9nz43v"}],"assertionMethod":["#initialKey"],"capabilityDelegation":["#initialKey"],"capabilityInvocation":["#initialKey"],"authentication":["#initialKey"],"id":"did:btcr2:_","@context":["https://www.w3.org/ns/did/v1.1","https://btcr2.dev/context/v1"]},"updates":[{"@context":["https://btcr2.dev/context/v1","https://w3id.org/json-ld-patch/v1","https://w3id.org/zcap/v1","https://w3id.org/security/data-integrity/v2"],"patch":[{"op":"add","path":"/service/4","value":{"id":"#dwn","type":"DecentralizedWebNode","serviceEndpoint":"http://example.com/dwn"}}],"sourceHash":"BMSG8nZwgYom7x_Qvmq9o9LGPVpWt16yg_MHf4GhTXw","targetHash":"j0NuymP5pkGYvmstuW0UxIl0siHAuKfZIqiT_bSa7ns","targetVersionId":2,"proof":{"type":"DataIntegrityProof","cryptosuite":"bip340-jcs-2025","verificationMethod":"#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ax1qkmymt685xkmzempysshq7ph56x5v9w6973nekgvmsjh3nn05eghylfh0my","capabilityAction":"Write","proofValue":"z3xB6XH5HVJPjb3ciph65yrhB8KfNaq5oJr5eNkPoj6ZAk7fwEwQ1qzFY28xCF5LhMPSpTEj3EEe918Fjp5F3sENS"}}]}}'
```

Genesis document-based (from CAS) did:btcr2 on Mutinynet without Update:

```
curl -X GET 'http://localhost:8080/1.0/identifiers/did:btcr2:x1q4n7jq922q0s3cejckmjyxlx9z8mtl3x6fusgz8xlu7jc69sqefn79wmhrc'
```

Genesis document-based (from CAS) did:btcr2 on Mutinynet with 3 Updates (from CAS):

```
curl -X GET 'http://localhost:8080/1.0/identifiers/did:btcr2:x1q4ds0xsls4a5kt2fgh2lcjqtjd7rmcgw66ts2p0la3pqh6k4vzegjvqlr0c'
```

Public Key-based did:btcr2 on Mutinynet with Deactivate (from Sidecar):

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:k1q5plamr904xqqdh96hnxjrcmuhyg2a466dggcvzmmpwgnl28r25dwmspg7d8h' -d '{"sidecar":{"updates":[{"@context":["https://btcr2.dev/context/v1","https://w3id.org/json-ld-patch/v1","https://w3id.org/zcap/v1","https://w3id.org/security/data-integrity/v2"],"patch":[{"op":"add","path":"/deactivated","value":true}],"sourceHash":"-EN6_rl8XRZzAhpbFRDowUWuMe_tQXNclIg7KhovxGw","targetHash":"TFXmM2sA5O-tGh-1fgVT9HBVG4aCh635ei8vQZnznxQ","targetVersionId":2,"proof":{"type":"DataIntegrityProof","cryptosuite":"bip340-jcs-2025","verificationMethod":"did:btcr2:k1q5plamr904xqqdh96hnxjrcmuhyg2a466dggcvzmmpwgnl28r25dwmspg7d8h#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ak1q5plamr904xqqdh96hnxjrcmuhyg2a466dggcvzmmpwgnl28r25dwmspg7d8h","capabilityAction":"Write","proofValue":"z3LGpf1RpP8HeVEFfX7PWuUmmVusn48yoDjZnDKGsmyb9xz4PScVTSVbhaZxRcaP8FKHmiAPtFY7jc6nYFLj92vth"}}]}}'
```

Genesis document-based (from CAS) did:btcr2 on Mutinynet with 1 Update (from CAS) and Deactivate (from CAS):

```
curl -X GET 'http://localhost:8080/1.0/identifiers/did:btcr2:x1qh8s79qs6k35dqu6ypq7hk6czhmfa5fhtc37v5846p7hud87rrhsytukexk'
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
