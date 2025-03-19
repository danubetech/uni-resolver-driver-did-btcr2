# Universal Resolver Driver: did:btc1

This is a [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/) driver for **did:btc1** identifiers.

(work in progress)

## Specifications

* [Decentralized Identifiers](https://w3c.github.io/did-core/)
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

### `uniresolver_driver_did_btc1_bitcoinConnection`

 * Specifies how the driver interacts with the Bitcoin blockchain.
 * Possible values: 
   * `bitcoind`: Connects to a [bitcoind](https://bitcoin.org/en/full-node) instance via JSON-RPC
   * `btcd`: Connects to a [btcd](https://github.com/btcsuite/btcd) instance via JSON-RPC
   * `bitcoinj`: Connects to Bitcoin using a local [bitcoinj](https://bitcoinj.github.io/) client
   * `blockcypherapi`: Connects to [BlockCypher's API](https://www.blockcypher.com/dev/bitcoin/)
 * Default value: `blockcypherapi`

### `uniresolver_driver_did_btc1_rpcUrlMainnet`

 * Specifies the JSON-RPC URL of a bitcoind/btcd instance running on Mainnet.
 * Default value: `http://user:pass@localhost:8332/`

### `uniresolver_driver_did_btc1_rpcUrlTestnet`

 * Specifies the JSON-RPC URL of a bitcoind/btcd instance running on Testnet.
 * Default value: `http://user:pass@localhost:18332/`

### `uniresolver_driver_did_btc1_rpcCertMainnet`

 * Specifies the server TLS certificate of the bitcoind/btcd instance running on Mainnet.
 * Default value: ``

### `uniresolver_driver_did_btc1_rpcCertTestnet`

 * Specifies the server TLS certificate of the bitcoind/btcd instance running on Testnet.
 * Default value: ``

## Driver Metadata

The driver returns the following metadata in addition to a DID document:

* `blockHeight`: ...
* `blockIndex`: ...
* TODO
