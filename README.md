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
curl -g -X GET 'http://localhost:8080/1.0/identifiers/did%3Abtc1%3Ak1q5pa5tq86fzrl0ez32nh8e0ks4tzzkxnnmn8tdvxk04ahzt70u09dag02h0cp?%7B%22sidecarData%22%3A%7B%22did%22%3A%22did%3Abtc1%3Ak1q5pa5tq86fzrl0ez32nh8e0ks4tzzkxnnmn8tdvxk04ahzt70u09dag02h0cp%22%2C%22signalsMetadata%22%3A%7B%22d1fd936f1ddffd5ebf630158d2b92fe07be41f13c2c7ee378baf7dbb0337e14d%22%3A%7B%22updatePayload%22%3A%7B%22%40context%22%3A%5B%22https%3A%2F%2Fw3id.org%2Fsecurity%2Fv2%22%2C%22https%3A%2F%2Fw3id.org%2Fzcap%2Fv1%22%2C%22https%3A%2F%2Fw3id.org%2Fjson-ld-patch%2Fv1%22%5D%2C%22patch%22%3A%5B%7B%22op%22%3A%22add%22%2C%22path%22%3A%22%2FverificationMethod%2F1%22%2C%22value%22%3A%7B%22id%22%3A%22did%3Abtc1%3Ak1q5pa5tq86fzrl0ez32nh8e0ks4tzzkxnnmn8tdvxk04ahzt70u09dag02h0cp%23key-1%22%2C%22type%22%3A%22Multikey%22%2C%22controller%22%3A%22did%3Abtc1%3Ak1q5pa5tq86fzrl0ez32nh8e0ks4tzzkxnnmn8tdvxk04ahzt70u09dag02h0cp%22%2C%22publicKeyMultibase%22%3A%22zQ3shnJ534uiR8xHXZuumXDDeAYwJhkTbiyYcaFSbwWxCnpXT%22%7D%7D%5D%2C%22sourceHash%22%3A%228SZT2CphokAD389BtLmn5DGY6QUbM5QBSnFqpdzAwdaU%22%2C%22targetHash%22%3A%223YgLJ5dshQzrfy3ZjiH6TFWtwjQP8XGirjzT5ysmTh5m%22%2C%22targetVersionId%22%3A2%2C%22proof%22%3A%7B%22type%22%3A%22DataIntegrityProof%22%2C%22cryptosuite%22%3A%22bip340-jcs-2025%22%2C%22verificationMethod%22%3A%22did%3Abtc1%3Ak1q5pa5tq86fzrl0ez32nh8e0ks4tzzkxnnmn8tdvxk04ahzt70u09dag02h0cp%23initialKey%22%2C%22proofPurpose%22%3A%22capabilityInvocation%22%2C%22capability%22%3A%22urn%3Azcap%3Aroot%3Adid%253Abtc1%253Ak1q5pa5tq86fzrl0ez32nh8e0ks4tzzkxnnmn8tdvxk04ahzt70u09dag02h0cp%22%2C%22capabilityAction%22%3A%22Write%22%2C%22%40context%22%3A%5B%22https%3A%2F%2Fw3id.org%2Fsecurity%2Fv2%22%2C%22https%3A%2F%2Fw3id.org%2Fzcap%2Fv1%22%2C%22https%3A%2F%2Fw3id.org%2Fjson-ld-patch%2Fv1%22%5D%2C%22proofValue%22%3A%22z2mEjk9a1cgm9KZyo16heWmuoSV9TZYaQmuKkJMoh4pvFYhQnaz18WAWNv98MQ9xqbWa95rYoMCvEy5c2wc2yaFYg%22%7D%7D%7D%2C%22403e8a168e2e33dda6274021975ce9ea38de72d481cd7674507020ac435890fa%22%3A%7B%22updatePayload%22%3A%7B%22%40context%22%3A%5B%22https%3A%2F%2Fw3id.org%2Fsecurity%2Fv2%22%2C%22https%3A%2F%2Fw3id.org%2Fzcap%2Fv1%22%2C%22https%3A%2F%2Fw3id.org%2Fjson-ld-patch%2Fv1%22%5D%2C%22patch%22%3A%5B%7B%22op%22%3A%22add%22%2C%22path%22%3A%22%2Fservice%2F3%22%2C%22value%22%3A%7B%22id%22%3A%22did%3Abtc1%3Ak1q5pa5tq86fzrl0ez32nh8e0ks4tzzkxnnmn8tdvxk04ahzt70u09dag02h0cp%23service-3%22%2C%22type%22%3A%22SingletonBeacon%22%2C%22serviceEndpoint%22%3A%22bitcoin%3Atb1qcs60r4j6ema8x4gf07hgt83x45e650dr97q3qv%22%7D%7D%5D%2C%22sourceHash%22%3A%223YgLJ5dshQzrfy3ZjiH6TFWtwjQP8XGirjzT5ysmTh5m%22%2C%22targetHash%22%3A%22GceTr5Ac4c3LK4uySTtiyBm2W7bmQHAsNCbGKbeNCAgg%22%2C%22targetVersionId%22%3A3%2C%22proof%22%3A%7B%22type%22%3A%22DataIntegrityProof%22%2C%22cryptosuite%22%3A%22bip340-jcs-2025%22%2C%22verificationMethod%22%3A%22did%3Abtc1%3Ak1q5pa5tq86fzrl0ez32nh8e0ks4tzzkxnnmn8tdvxk04ahzt70u09dag02h0cp%23initialKey%22%2C%22proofPurpose%22%3A%22capabilityInvocation%22%2C%22capability%22%3A%22urn%3Azcap%3Aroot%3Adid%253Abtc1%253Ak1q5pa5tq86fzrl0ez32nh8e0ks4tzzkxnnmn8tdvxk04ahzt70u09dag02h0cp%22%2C%22capabilityAction%22%3A%22Write%22%2C%22%40context%22%3A%5B%22https%3A%2F%2Fw3id.org%2Fsecurity%2Fv2%22%2C%22https%3A%2F%2Fw3id.org%2Fzcap%2Fv1%22%2C%22https%3A%2F%2Fw3id.org%2Fjson-ld-patch%2Fv1%22%5D%2C%22proofValue%22%3A%22z4WtnwesES1oTXLDPTJdwQEAkvStsWKxW8FFbnKt2SRh6QRdcGFk5DiEdG7QWoVDXqCRX3FpgrrTKhebEnuLHxbrf%22%7D%7D%7D%7D%7D%7D'
curl -g -X GET 'http://localhost:8080/1.0/identifiers/did%3Abtc1%3Ak1qypcylxwhf8sykn2dztm6z8lxm43kwkyzf07qmp9jafv3zfntmpwtks9hmnrw?%7B%22targetTime%22%3A1748445840000%2C%22sidecarData%22%3A%7B%22did%22%3A%22did%3Abtc1%3Ak1qypcylxwhf8sykn2dztm6z8lxm43kwkyzf07qmp9jafv3zfntmpwtks9hmnrw%22%2C%22signalsMetadata%22%3A%7B%2274de8650981e34ec3fe627a0ac289753a614499e2905e8a2e4f56475b6630b9b%22%3A%7B%22updatePayload%22%3A%7B%22%40context%22%3A%5B%22https%3A%2F%2Fw3id.org%2Fsecurity%2Fv2%22%2C%22https%3A%2F%2Fw3id.org%2Fzcap%2Fv1%22%2C%22https%3A%2F%2Fw3id.org%2Fjson-ld-patch%2Fv1%22%5D%2C%22patch%22%3A%5B%7B%22op%22%3A%22replace%22%2C%22path%22%3A%22%2Fservice%2F0%22%2C%22value%22%3A%7B%22id%22%3A%22did%3Abtc1%3Ak1qypcylxwhf8sykn2dztm6z8lxm43kwkyzf07qmp9jafv3zfntmpwtks9hmnrw%23initialP2PKH%22%2C%22type%22%3A%22SingletonBeacon%22%2C%22serviceEndpoint%22%3A%22bitcoin%3An19usRiN4nihajeQCt2EYXXSehp4hMFdAX%22%7D%7D%5D%2C%22targetHash%22%3A%223gVCsXG1WCbxHngKo6yE74czmshbjh6tvfNd6DqXASiq%22%2C%22targetVersionId%22%3A2%2C%22sourceHash%22%3A%22CSAGnUTVviHe6Wb2d3LATrdDJgVrJQxX3JzNSmTigBHF%22%2C%22proof%22%3A%7B%22cryptosuite%22%3A%22bip340-jcs-2025%22%2C%22type%22%3A%22DataIntegrityProof%22%2C%22verificationMethod%22%3A%22did%3Abtc1%3Ak1qypcylxwhf8sykn2dztm6z8lxm43kwkyzf07qmp9jafv3zfntmpwtks9hmnrw%23initialKey%22%2C%22proofPurpose%22%3A%22capabilityInvocation%22%2C%22capability%22%3A%22urn%3Azcap%3Aroot%3Adid%253Abtc1%253Ak1qypcylxwhf8sykn2dztm6z8lxm43kwkyzf07qmp9jafv3zfntmpwtks9hmnrw%22%2C%22capabilityAction%22%3A%22Write%22%2C%22%40context%22%3A%5B%22https%3A%2F%2Fw3id.org%2Fsecurity%2Fv2%22%2C%22https%3A%2F%2Fw3id.org%2Fzcap%2Fv1%22%2C%22https%3A%2F%2Fw3id.org%2Fjson-ld-patch%2Fv1%22%5D%2C%22proofValue%22%3A%22zccdDHA7hcomGAgP5BPtERKuJGa7QGKgPadauESUNQ8u7eJFQwacm5KEK9sWMymGXK8YxgxPPdmgBZT3bGh4wp6q%22%7D%7D%7D%7D%7D%7D'
curl -g -X GET 'http://localhost:8080/1.0/identifiers/did%3Abtc1%3Ak1qypdnfyh7j8z87wk3vylqaz9t8psnkws8k5e2ccl9c0zqwwt5uyjeeg7f3knj'
```
	
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
