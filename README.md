# Universal Resolver Driver: did:btc1

This is a [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/) driver for **did:btc1** identifiers.

(work in progress)

## Specifications

* [Decentralized Identifiers](https://www.w3.org/TR/did-1.0/)
* [DID Method Specification](https://dcdpr.github.io/did-btc1/)

## Example DIDs

```
 did:btc1:regtest:k1qvadgpl5qfuz6emq7c8sqw28z0r0gzvyra3je3pp2cuk83uqnnyvckvw8cf
 did:btc1:TODO
 did:btc1:TODO
```

## Build and Run (Docker)

```
docker build -f ./docker/Dockerfile . -t universalresolver/driver-did-btc1
docker run -p 8080:8080 universalresolver/driver-did-btc1
curl -X GET http://localhost:8080/1.0/identifiers/did:btc1:TODO
```

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
