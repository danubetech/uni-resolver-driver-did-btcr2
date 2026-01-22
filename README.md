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

```
curl -X GET 'http://localhost:8080/1.0/identifiers/did:btcr2:k1q5ppmnfjqp0qe5klmnll9tazz9jd5ds43x5xfsr3hu9jdgaldu0d3jgs0vj4r'
curl -X GET 'http://localhost:8080/1.0/identifiers/did%3Abtcr2%3Ak1q5pua0p3syhn3p3kpvuqkx7sxd9ndv6uffwvuv008n4nq6fdwv22x5q4qfp5h?%7B%22sidecarData%22%3A%7B%22did%22%3A%22did%3Abtcr2%3Ak1q5pua0p3syhn3p3kpvuqkx7sxd9ndv6uffwvuv008n4nq6fdwv22x5q4qfp5h%22%2C%22signalsMetadata%22%3A%7B%222910c61eff717307e13f2f150156f566bacb96dccbad5f8959874c4d7ac94faf%22%3A%7B%22didUpdate%22%3A%7B%22%40context%22%3A%5B%22https%3A%2F%2Fw3id.org%2Fsecurity%2Fv2%22%2C%22https%3A%2F%2Fw3id.org%2Fzcap%2Fv1%22%2C%22https%3A%2F%2Fw3id.org%2Fjson-ld-patch%2Fv1%22%5D%2C%22patch%22%3A%5B%7B%22op%22%3A%22add%22%2C%22path%22%3A%22%2Fservice%2F3%22%2C%22value%22%3A%7B%22type%22%3A%22SingletonBeacon%22%2C%22id%22%3A%22did%3Abtcr2%3Ak1q5pua0p3syhn3p3kpvuqkx7sxd9ndv6uffwvuv008n4nq6fdwv22x5q4qfp5h%23initialP2PKH%22%2C%22serviceEndpoint%22%3A%22bitcoin%3Amx1d72m2yzbJiTCdjrJKJNDtANURL3BGDD%22%7D%7D%5D%2C%22targetHash%22%3A%222Lj51vMAcfaXWF6Hfm8gTac4PPUzT1m45AadqLeVbF5W%22%2C%22targetVersionId%22%3A2%2C%22sourceHash%22%3A%2234LDxujTRru6QRHZFEnN1i9sFvPiCexFSdCucVauEyFH%22%2C%22proof%22%3A%7B%22cryptosuite%22%3A%22bip340-jcs-2025%22%2C%22type%22%3A%22DataIntegrityProof%22%2C%22verificationMethod%22%3A%22did%3Abtcr2%3Ak1q5pua0p3syhn3p3kpvuqkx7sxd9ndv6uffwvuv008n4nq6fdwv22x5q4qfp5h%23initialKey%22%2C%22proofPurpose%22%3A%22capabilityInvocation%22%2C%22capability%22%3A%22urn%3Azcap%3Aroot%3Adid%253Abtcr2%253Ak1q5pua0p3syhn3p3kpvuqkx7sxd9ndv6uffwvuv008n4nq6fdwv22x5q4qfp5h%22%2C%22capabilityAction%22%3A%22Write%22%2C%22%40context%22%3A%5B%22https%3A%2F%2Fw3id.org%2Fsecurity%2Fv2%22%2C%22https%3A%2F%2Fw3id.org%2Fzcap%2Fv1%22%2C%22https%3A%2F%2Fw3id.org%2Fjson-ld-patch%2Fv1%22%5D%2C%22proofValue%22%3A%22z5BPFwuLAjnJmY7h2nPNFCneXHzxyusufNSfmw7a8SiwbiHxUyXVXaJyV5fL6ZPd397uuAVVF8f7F9syX2aYyXsQR%22%7D%7D%7D%7D%7D%7D'
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

 * Specifies the JSON-RPC URLs of the bitcoin connections.

### `uniresolver_driver_did_btcr2_bitcoinConnectionsCerts`

 * Specifies the server TLS certificates of the bitcoin connections.
 * Default value: ``

## Driver Metadata

The driver returns the following metadata in addition to a DID document:

* `blockHeight`: ...
* `blockIndex`: ...
