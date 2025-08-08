# Universal Resolver Driver: did:btc1

This is a [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/) driver for **did:btc1** identifiers.

(work in progress)

## Specifications

* [Decentralized Identifiers](https://www.w3.org/TR/did-1.0/)
* [DID Method Specification](https://dcdpr.github.io/did-btc1/)

## Build and Run (Docker)

```
docker compose build
docker compose up
```

## Example DIDs

```
curl -g -X GET 'http://localhost:8080/1.0/identifiers/did%3Abtc1%3Ak1qypcylxwhf8sykn2dztm6z8lxm43kwkyzf07qmp9jafv3zfntmpwtks9hmnrw?%7B%22targetTime%22%3A1748445840000%2C%22sidecarData%22%3A%7B%22did%22%3A%22did%3Abtc1%3Ak1qypcylxwhf8sykn2dztm6z8lxm43kwkyzf07qmp9jafv3zfntmpwtks9hmnrw%22%2C%22signalsMetadata%22%3A%7B%2274de8650981e34ec3fe627a0ac289753a614499e2905e8a2e4f56475b6630b9b%22%3A%7B%22updatePayload%22%3A%7B%22%40context%22%3A%5B%22https%3A%2F%2Fw3id.org%2Fsecurity%2Fv2%22%2C%22https%3A%2F%2Fw3id.org%2Fzcap%2Fv1%22%2C%22https%3A%2F%2Fw3id.org%2Fjson-ld-patch%2Fv1%22%5D%2C%22patch%22%3A%5B%7B%22op%22%3A%22replace%22%2C%22path%22%3A%22%2Fservice%2F0%22%2C%22value%22%3A%7B%22id%22%3A%22did%3Abtc1%3Ak1qypcylxwhf8sykn2dztm6z8lxm43kwkyzf07qmp9jafv3zfntmpwtks9hmnrw%23initialP2PKH%22%2C%22type%22%3A%22SingletonBeacon%22%2C%22serviceEndpoint%22%3A%22bitcoin%3An19usRiN4nihajeQCt2EYXXSehp4hMFdAX%22%7D%7D%5D%2C%22targetHash%22%3A%223gVCsXG1WCbxHngKo6yE74czmshbjh6tvfNd6DqXASiq%22%2C%22targetVersionId%22%3A2%2C%22sourceHash%22%3A%22CSAGnUTVviHe6Wb2d3LATrdDJgVrJQxX3JzNSmTigBHF%22%2C%22proof%22%3A%7B%22cryptosuite%22%3A%22bip340-jcs-2025%22%2C%22type%22%3A%22DataIntegrityProof%22%2C%22verificationMethod%22%3A%22did%3Abtc1%3Ak1qypcylxwhf8sykn2dztm6z8lxm43kwkyzf07qmp9jafv3zfntmpwtks9hmnrw%23initialKey%22%2C%22proofPurpose%22%3A%22capabilityInvocation%22%2C%22capability%22%3A%22urn%3Azcap%3Aroot%3Adid%253Abtc1%253Ak1qypcylxwhf8sykn2dztm6z8lxm43kwkyzf07qmp9jafv3zfntmpwtks9hmnrw%22%2C%22capabilityAction%22%3A%22Write%22%2C%22%40context%22%3A%5B%22https%3A%2F%2Fw3id.org%2Fsecurity%2Fv2%22%2C%22https%3A%2F%2Fw3id.org%2Fzcap%2Fv1%22%2C%22https%3A%2F%2Fw3id.org%2Fjson-ld-patch%2Fv1%22%5D%2C%22proofValue%22%3A%22zccdDHA7hcomGAgP5BPtERKuJGa7QGKgPadauESUNQ8u7eJFQwacm5KEK9sWMymGXK8YxgxPPdmgBZT3bGh4wp6q%22%7D%7D%7D%7D%7D%7D'
curl -g -X GET http://localhost:8080/1.0/identifiers/did%3Abtc1%3Ak1qypdnfyh7j8z87wk3vylqaz9t8psnkws8k5e2ccl9c0zqwwt5uyjeeg7f3knj
```

{"targetTime":1748445840000,"sidecarData":{"did":"did:btc1:k1qypcylxwhf8sykn2dztm6z8lxm43kwkyzf07qmp9jafv3zfntmpwtks9hmnrw","signalsMetadata":{"74de8650981e34ec3fe627a0ac289753a614499e2905e8a2e4f56475b6630b9b":{"updatePayload":{"@context":["https://w3id.org/security/v2","https://w3id.org/zcap/v1","https://w3id.org/json-ld-patch/v1"],"patch":[{"op":"replace","path":"/service/0","value":{"id":"did:btc1:k1qypcylxwhf8sykn2dztm6z8lxm43kwkyzf07qmp9jafv3zfntmpwtks9hmnrw#initialP2PKH","type":"SingletonBeacon","serviceEndpoint":"bitcoin:n19usRiN4nihajeQCt2EYXXSehp4hMFdAX"}}],"targetHash":"3gVCsXG1WCbxHngKo6yE74czmshbjh6tvfNd6DqXASiq","targetVersionId":2,"sourceHash":"CSAGnUTVviHe6Wb2d3LATrdDJgVrJQxX3JzNSmTigBHF","proof":{"cryptosuite":"bip340-jcs-2025","type":"DataIntegrityProof","verificationMethod":"did:btc1:k1qypcylxwhf8sykn2dztm6z8lxm43kwkyzf07qmp9jafv3zfntmpwtks9hmnrw#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtc1%3Ak1qypcylxwhf8sykn2dztm6z8lxm43kwkyzf07qmp9jafv3zfntmpwtks9hmnrw","capabilityAction":"Write","@context":["https://w3id.org/security/v2","https://w3id.org/zcap/v1","https://w3id.org/json-ld-patch/v1"],"proofValue":"zccdDHA7hcomGAgP5BPtERKuJGa7QGKgPadauESUNQ8u7eJFQwacm5KEK9sWMymGXK8YxgxPPdmgBZT3bGh4wp6q"}}}}}}


## Build (native Java)

	mvn clean install
	
## Driver Environment Variables

The driver recognizes the following environment variables:

### `uniresolver_driver_did_btc1_bitcoinConnections`

 * Specifies how the driver interacts with the Bitcoin blockchain.
 * Possible values: 
   * `bitcoind`: Connects to a [bitcoind](https://bitcoin.org/en/full-node) instance via JSON-RPC
   * `btcd`: Connects to a [btcd](https://github.com/btcsuite/btcd) instance via JSON-RPC
   * `bitcoinj`: Connects to Bitcoin using a local [bitcoinj](https://bitcoinj.github.io/) client
   * `blockcypherapi`: Connects to [BlockCypher's API](https://www.blockcypher.com/dev/bitcoin/)
   * `esploraelectrsrest`: Connects to Esplora/Electrs REST API
 * Default value: `bitcoind`

### `uniresolver_driver_did_btc1_bitcoinConnectionsUrls`

 * Specifies the JSON-RPC URLs of the bitcoin connections.

### `uniresolver_driver_did_btc1_bitcoinConnectionsCerts`

 * Specifies the server TLS certificates of the bitcoin connections.
 * Default value: ``

## Driver Metadata

The driver returns the following metadata in addition to a DID document:

* `blockHeight`: ...
* `blockIndex`: ...
* TODO
