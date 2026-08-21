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

## Example DIDs

*Note: Additional data related to these examples is available under [./example-dids/](./example-dids/).*

**Example 1:** Public Key-based did:btcr2 on Mutinynet without Update:

```
curl -X GET 'http://localhost:8080/1.0/identifiers/did:btcr2:k1q5ppmnfjqp0qe5klmnll9tazz9jd5ds43x5xfsr3hu9jdgaldu0d3jgs0vj4r'
```

**Example 2:** Public Key-based did:btcr2 on Mutinynet with 1 Update (from Sidecar):

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:k1q5p7drc8y5hhmvs2nncyuq73ts98arnqv5ce446vwydafuu2mp9rp6szethjk' -d '{"sidecar":{"updates":[{"@context":["https://btcr2.dev/context/v1","https://w3id.org/json-ld-patch/v1","https://w3id.org/zcap/v1","https://w3id.org/security/data-integrity/v2"],"patch":[{"op":"add","path":"/service/3","value":{"id":"#didcomm","type":"DIDCommMessaging","serviceEndpoint":"http://example.com/didcomm/"}}],"sourceHash":"osaUrsl3XhLlm-J4hKrqmxP6G0y9sfqseaF4HPPUe_8","targetHash":"eS9wA7p1qiQHGEuyz4swjnZNnFz9VkBSPff6i6XeP_M","targetVersionId":2,"proof":{"type":"DataIntegrityProof","cryptosuite":"bip340-jcs-2025","verificationMethod":"did:btcr2:k1q5p7drc8y5hhmvs2nncyuq73ts98arnqv5ce446vwydafuu2mp9rp6szethjk#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ak1q5p7drc8y5hhmvs2nncyuq73ts98arnqv5ce446vwydafuu2mp9rp6szethjk","capabilityAction":"Write","proofValue":"z5DhhjMk8gXULWNKRwqJqGbqZF94ov7zjKS6YkaCGxoDa2f3WT3zprMUc1p62w2cRRdkNAhR4rfGJpxZZFfHsdrJB"}}]}}'
```

**Example 3:** Genesis document-based (from Sidecar) did:btcr2 on Mutinynet without Update:

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1qhcxze0km2e883qex8vr45u8d4v04lmmx2zkjm3mvmtj8a9wtnny5cc2l2q' -d '{"sidecar":{"genesisDocument":{"verificationMethod":[{"type":"Multikey","id":"#initialKey","publicKeyMultibase":"zQ3shqQAyNAMywkPs8xs3boi3GgzaDs7YV5PcK67faeKn7fJJ","controller":"did:btcr2:_"}],"service":[{"id":"#didcomm","type":"DIDCommMessaging","serviceEndpoint":"http://example.com/didcomm"}],"assertionMethod":["#initialKey"],"capabilityDelegation":["#initialKey"],"capabilityInvocation":["#initialKey"],"authentication":["#initialKey"],"id":"did:btcr2:_","@context":["https://www.w3.org/ns/did/v1.1","https://btcr2.dev/context/v1"]}}}'
```

**Example 4:** Genesis document-based (from Sidecar) did:btcr2 on Mutinynet with 1 Update (from Sidecar):

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1qkel9rl0ltz6w5m3rypnsa4tncu5yst45qdsmwtms94zx6wm7cc2q8nnfh7' -d '{"sidecar":{"genesisDocument":{"verificationMethod":[{"type":"Multikey","id":"#initialKey","publicKeyMultibase":"zQ3shQvp7YmdxSZMHYWvfD5GvoavZz4REJ5P4Snw6Qy2PVN1o","controller":"did:btcr2:_"}],"service":[{"id":"#didcomm","type":"DIDCommMessaging","serviceEndpoint":"http://example.com/didcomm"},{"type":"SingletonBeacon","id":"#initialP2PKH","serviceEndpoint":"bitcoin:mwSrpBnrNZp1uWat1hf2dynpWKs7JWF518"},{"type":"SingletonBeacon","id":"#initialP2WPKH","serviceEndpoint":"bitcoin:tb1q46auvxdypkjt75ny4n99v97j95hz592g675nyq"},{"type":"SingletonBeacon","id":"#initialP2TR","serviceEndpoint":"bitcoin:tb1pj70k34zj0fnf7wlqdvpm93aesyg496kjaws9cyemaqhnggp8cp9qx7c4je"}],"assertionMethod":["#initialKey"],"capabilityDelegation":["#initialKey"],"capabilityInvocation":["#initialKey"],"authentication":["#initialKey"],"id":"did:btcr2:_","@context":["https://www.w3.org/ns/did/v1.1","https://btcr2.dev/context/v1"]},"updates":[{"@context":["https://btcr2.dev/context/v1","https://w3id.org/json-ld-patch/v1","https://w3id.org/zcap/v1","https://w3id.org/security/data-integrity/v2"],"patch":[{"op":"add","path":"/service/4","value":{"id":"#dwn","type":"DecentralizedWebNode","serviceEndpoint":"http://example.com/dwn"}}],"sourceHash":"AC_466VA2q_trSzux771a0a1a9ynBc2LT7Nf8m0Zido","targetHash":"jqdZFDnOP9Ftu4lOhBRwPINoneKy7p6vLnhwlLjHQmI","targetVersionId":2,"proof":{"type":"DataIntegrityProof","cryptosuite":"bip340-jcs-2025","verificationMethod":"did:btcr2:x1qkel9rl0ltz6w5m3rypnsa4tncu5yst45qdsmwtms94zx6wm7cc2q8nnfh7#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ax1qkel9rl0ltz6w5m3rypnsa4tncu5yst45qdsmwtms94zx6wm7cc2q8nnfh7","capabilityAction":"Write","proofValue":"z3XzDFYWd3jNgVGPf1Hk2JXJZA1JE4aBE5GHrurgsAisp5AgLapXPdLvmXok7YJrXWEaCLe9TTyYNrnimGkUPoqU9"}}]}}'
```

**Example 5:** Genesis document-based (from CAS) did:btcr2 on Mutinynet without Update:

```
curl -X GET 'http://localhost:8080/1.0/identifiers/did:btcr2:x1q4n7jq922q0s3cejckmjyxlx9z8mtl3x6fusgz8xlu7jc69sqefn79wmhrc'
```

**Example 6:** Genesis document-based (from CAS) did:btcr2 on Mutinynet with 3 Updates (from CAS):

```
curl -X GET 'http://localhost:8080/1.0/identifiers/did:btcr2:x1q4tpl8hpeyr2et0lzeqsr0pakjmh796ry98vgvms9gw7fsk7eg302llx8ne'
```

**Example 7:** Public Key-based did:btcr2 on Mutinynet with Deactivate (from Sidecar):

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:k1q5plamr904xqqdh96hnxjrcmuhyg2a466dggcvzmmpwgnl28r25dwmspg7d8h' -d '{"sidecar":{"updates":[{"@context":["https://btcr2.dev/context/v1","https://w3id.org/json-ld-patch/v1","https://w3id.org/zcap/v1","https://w3id.org/security/data-integrity/v2"],"patch":[{"op":"add","path":"/deactivated","value":true}],"sourceHash":"-EN6_rl8XRZzAhpbFRDowUWuMe_tQXNclIg7KhovxGw","targetHash":"TFXmM2sA5O-tGh-1fgVT9HBVG4aCh635ei8vQZnznxQ","targetVersionId":2,"proof":{"type":"DataIntegrityProof","cryptosuite":"bip340-jcs-2025","verificationMethod":"did:btcr2:k1q5plamr904xqqdh96hnxjrcmuhyg2a466dggcvzmmpwgnl28r25dwmspg7d8h#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ak1q5plamr904xqqdh96hnxjrcmuhyg2a466dggcvzmmpwgnl28r25dwmspg7d8h","capabilityAction":"Write","proofValue":"z3LGpf1RpP8HeVEFfX7PWuUmmVusn48yoDjZnDKGsmyb9xz4PScVTSVbhaZxRcaP8FKHmiAPtFY7jc6nYFLj92vth"}}]}}'
```

**Example 8:** Genesis document-based (from CAS) did:btcr2 on Mutinynet with 1 Update (from CAS) and Deactivate (from CAS):

```
curl -X GET 'http://localhost:8080/1.0/identifiers/did:btcr2:x1q465l26md3gu8y8fzxcae3wzx9q9qntrrzsu6p9elx68e2wu03flzemt2zq'
```

**Example 9a:** Genesis document-based (from CAS) did:btcr2 on Mutinynet with 1 Update (from CAS) using CAS Announcement Map (from CAS):

*Note: This example uses the same block, transaction, and CAS Announcement Map as Example 9b.*

```
curl -X GET 'http://localhost:8080/1.0/identifiers/did:btcr2:x1q5pxp24p923v36m8kfzfd3cwtpsgpl5ackq23uvph2qsrzjhsrq57gnv5yk'
```

**Example 9b:** Genesis document-based (from CAS) did:btcr2 on Mutinynet with 1 Update (from CAS) using CAS Announcement Map (from CAS):

*Note: This example uses the same block, transaction, and CAS Announcement Map as Example 9a.*

```
curl -X GET 'http://localhost:8080/1.0/identifiers/did:btcr2:x1q587lk56v2rfnlpzerknx5tlht03c3j2gd27mvj7gqthjym9fn425rc9ygv'
```

**Example 10a:** Genesis document-based (from Sidecar) did:btcr2 on Mutinynet with 1 Update (from Sidecar) using CAS Announcement Map (from Sidecar):

*Note: This example uses the same block, transaction, and CAS Announcement Map as Example 10b.*

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1qhn7xvy3lhau0jy3e9n5klayh2vcyv07txu553eckw6gezn8vfrayduehz8' -d '{"sidecar":{"genesisDocument":{"verificationMethod":[{"type":"Multikey","id":"#initialKey","publicKeyMultibase":"zQ3shaL4Nxb1u7ri88ba27o9jSknJV91Gbe6Cbebpyc87B635","controller":"did:btcr2:_"}],"assertionMethod":["#initialKey"],"capabilityDelegation":["#initialKey"],"capabilityInvocation":["#initialKey"],"authentication":["#initialKey"],"id":"did:btcr2:_","@context":["https://www.w3.org/ns/did/v1.1","https://btcr2.dev/context/v1"],"service":[{"type":"SingletonBeacon","id":"#initialP2PKH","serviceEndpoint":"bitcoin:mkiWTfbehzMpv7BvEb6ar64wEzo1ic3RKH"},{"type":"SingletonBeacon","id":"#initialP2WPKH","serviceEndpoint":"bitcoin:tb1q8yyquxkj90zmjpqu9wjmzwvx5ea709dkkgjzqg"},{"type":"SingletonBeacon","id":"#initialP2TR","serviceEndpoint":"bitcoin:tb1ph0z3j7k380hrm90x0z5afgz9yv79mmgs5ljel068c9dcrujdvensn2n6qz"},{"type":"CASBeacon","id":"#cohort-mutinynet-cas-2","serviceEndpoint":"bitcoin:tb1pd79gln669alnp86d3tcffqa77effk83v0exja368sa02fyxglxcqhwcj9l"}]},"updates":[{"@context":["https://btcr2.dev/context/v1","https://w3id.org/json-ld-patch/v1","https://w3id.org/zcap/v1","https://w3id.org/security/data-integrity/v2"],"patch":[{"op":"add","path":"/service/4","value":{"id":"#dwn","type":"DecentralizedWebNode","serviceEndpoint":"http://example.com/dwn"}}],"sourceHash":"gVAhXy63r_pYljv7ahQkNymdZW_Q1sQQ8UvNqTWEAKU","targetHash":"DRTduk6nIdcyEdmoVnPp_zN7tqTgeH6lPWRanYzcK9s","targetVersionId":2,"proof":{"type":"DataIntegrityProof","cryptosuite":"bip340-jcs-2025","verificationMethod":"did:btcr2:x1qhn7xvy3lhau0jy3e9n5klayh2vcyv07txu553eckw6gezn8vfrayduehz8#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ax1qhn7xvy3lhau0jy3e9n5klayh2vcyv07txu553eckw6gezn8vfrayduehz8","capabilityAction":"Write","proofValue":"zoYWD1syitvDRcbjT4UcAKyw6QJJ8DMkzDdAnt6CfcjyVsvp9A6r8kHKiPNaqFPe3iHxtQRDrQfS8wsAVja5qvGd"}}],"casUpdates":[{"did:btcr2:x1qkvj2tupjlsntl5c6he47h2gjhwr6c5ndvp3u92s4a6hs7qec5s97run2aw":"vfRTPYVzLD8CRnadTEMXkkagbrxIrmHZT3DKAvFruFA","did:btcr2:x1qhn7xvy3lhau0jy3e9n5klayh2vcyv07txu553eckw6gezn8vfrayduehz8":"RvFuBbfT57Y3xy4GbtWpSfJMfQ03IBs2Ksjdcxy1a0g"}]}}'
```

**Example 10b:** Genesis document-based (from Sidecar) did:btcr2 on Mutinynet with 1 Update (from Sidecar) using CAS Announcement Map (from Sidecar):

*Note: This example uses the same block, transaction, and CAS Announcement Map as Example 10a.*

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1qkvj2tupjlsntl5c6he47h2gjhwr6c5ndvp3u92s4a6hs7qec5s97run2aw' -d '{"sidecar":{"genesisDocument":{"verificationMethod":[{"type":"Multikey","id":"#initialKey","publicKeyMultibase":"zQ3shsMVg4SAQNb561q2wLhuhQe3yv1WZXWx7zsm1k56iUsSB","controller":"did:btcr2:_"}],"assertionMethod":["#initialKey"],"capabilityDelegation":["#initialKey"],"capabilityInvocation":["#initialKey"],"authentication":["#initialKey"],"id":"did:btcr2:_","@context":["https://www.w3.org/ns/did/v1.1","https://btcr2.dev/context/v1"],"service":[{"type":"SingletonBeacon","id":"#initialP2PKH","serviceEndpoint":"bitcoin:my9vSV2TjCLhKGLLpXzYAfHexP5x51Kjdd"},{"type":"SingletonBeacon","id":"#initialP2WPKH","serviceEndpoint":"bitcoin:tb1qc9mlkyy6gtye4fxsrqts2m2erg0mpg0yxvc00j"},{"type":"SingletonBeacon","id":"#initialP2TR","serviceEndpoint":"bitcoin:tb1ps3g7sys25a443cx76cs4t6ekedzazt33f6qt25m965xskg3cwezqzyhu80"},{"type":"CASBeacon","id":"#cohort-mutinynet-cas-2","serviceEndpoint":"bitcoin:tb1pd79gln669alnp86d3tcffqa77effk83v0exja368sa02fyxglxcqhwcj9l"}]},"updates":[{"@context":["https://btcr2.dev/context/v1","https://w3id.org/json-ld-patch/v1","https://w3id.org/zcap/v1","https://w3id.org/security/data-integrity/v2"],"patch":[{"op":"add","path":"/service/4","value":{"id":"#didcomm","type":"DIDCommMessaging","serviceEndpoint":"http://example.com/didcomm"}}],"sourceHash":"5a5xJPNsPT7KEWu5mWEw8dAISSCdMFOwylrbIfTUL94","targetHash":"QtUyma1MgFeHMRerJDejRQ2PrzK2IFYYAwGNC8sDvlE","targetVersionId":2,"proof":{"type":"DataIntegrityProof","cryptosuite":"bip340-jcs-2025","verificationMethod":"did:btcr2:x1qkvj2tupjlsntl5c6he47h2gjhwr6c5ndvp3u92s4a6hs7qec5s97run2aw#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ax1qkvj2tupjlsntl5c6he47h2gjhwr6c5ndvp3u92s4a6hs7qec5s97run2aw","capabilityAction":"Write","proofValue":"z21oPYfxPK92FDFjLimot8AVPrrACVWcVPYABFhVWxmxxg6nUNmqresnqVdBQHjL9dVLt9eQL6XYdh9net9rVbP3N"}}],"casUpdates":[{"did:btcr2:x1qkvj2tupjlsntl5c6he47h2gjhwr6c5ndvp3u92s4a6hs7qec5s97run2aw":"vfRTPYVzLD8CRnadTEMXkkagbrxIrmHZT3DKAvFruFA","did:btcr2:x1qhn7xvy3lhau0jy3e9n5klayh2vcyv07txu553eckw6gezn8vfrayduehz8":"RvFuBbfT57Y3xy4GbtWpSfJMfQ03IBs2Ksjdcxy1a0g"}]}}'
```

**Example 11a:** Genesis document-based (from CAS) did:btcr2 on Mutinynet with 1 Update (from CAS) using SMT Proof (from Sidecar):

*Note: This example uses the same block, transaction, and Sparse Merkle Tree as Example 11b.*

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1qh8jfp5a4xz99dyu4u3z0ht7c89u07utdajzlljcwgrgwcgqc0jdkjgxe2t' -d '{"sidecar":{"smtProofs":[{"id":"qCEc5kyLneJDpX084JS96w9kyx36h1E7x_HdjTyaLpo","nonce":"Hv3vOQuADfvKSBCbDQ8qt_jS5SBmFODAVl6Dnff4UWs","updateId":"rq0xO0K-49p_l_15-QD8bYkfWH1-_5Vx2iiYlhDuBEI","collapsed":"__________________________________________0","hashes":["psXw4HNvxAotqS50R_AvyYhhUgTKGn1ANQmcI-Jbqeg"]}]}}'
```

**Example 11b:** Genesis document-based (from CAS) did:btcr2 on Mutinynet with 1 Update (from CAS) using SMT Proof (from Sidecar):

*Note: This example uses the same block, transaction, and Sparse Merkle Tree as Example 11a.*

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1q4fk0ksarsf29jswcftc84wy25ckkxgvxx2ln8qhuf0c7h0rtltcvwmdyrc' -d '{"sidecar":{"smtProofs":[{"id":"qCEc5kyLneJDpX084JS96w9kyx36h1E7x_HdjTyaLpo","nonce":"W_Q8DhhMuxfaRISaFI53t9cj0p3NpY9MR1ijaHa7Q2M","updateId":"u5fOh4xtJ_FSsTn0eI7jyXlxUprYOpEc5rht0cla7zE","collapsed":"__________________________________________0","hashes":["KjAYe_6H7oo-JYHIkuM9pJ8fkGL1PpuZUQFeFwQ6QHU"]}]}}'
```

**Example 12a:** Genesis document-based (from Sidecar) did:btcr2 on Mutinynet with 1 Update (from Sidecar) using SMT Proof (from Sidecar):

*Note: This example uses the same block, transaction, and Sparse Merkle Tree as Example 12b.*

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1qhw2cawpshxnvlkxwx7ux2t7z78mzkz457r4z6m828nz4gplgcjekdgjv6t' -d '{"sidecar":{"genesisDocument":{"verificationMethod":[{"type":"Multikey","id":"#initialKey","publicKeyMultibase":"zQ3shsdxZQxp8pvmpfHtJD9omKikKZXzW8LBbXYwfbJo7c6Ef","controller":"did:btcr2:_"}],"assertionMethod":["#initialKey"],"capabilityDelegation":["#initialKey"],"capabilityInvocation":["#initialKey"],"authentication":["#initialKey"],"id":"did:btcr2:_","@context":["https://www.w3.org/ns/did/v1.1","https://btcr2.dev/context/v1"],"service":[{"type":"SingletonBeacon","id":"#initialP2PKH","serviceEndpoint":"bitcoin:n4rFYwMc6uG8FHwAg3K7ue1b2CTbUQUowj"},{"type":"SingletonBeacon","id":"#initialP2WPKH","serviceEndpoint":"bitcoin:tb1qllcn8rcsh02a2xs93trycqm2g5rxpw6fv4e72j"},{"type":"SingletonBeacon","id":"#initialP2TR","serviceEndpoint":"bitcoin:tb1pqukfq02r07jzw4km5vmcscpqgnzd6hyswwhn4vqmn0zj3xq973uqs626u7"},{"type":"SMTBeacon","id":"#cohort-mutinynet-smt-2","serviceEndpoint":"bitcoin:tb1phaymjanje6hhwusxpcuq9jm8zdavd2j074e5mhmdh3a2vjkzknjq76kg7c"}]},"updates":[{"@context":["https://btcr2.dev/context/v1","https://w3id.org/json-ld-patch/v1","https://w3id.org/zcap/v1","https://w3id.org/security/data-integrity/v2"],"patch":[{"op":"add","path":"/service/4","value":{"id":"#didcomm","type":"DIDCommMessaging","serviceEndpoint":"http://example.com/didcomm"}}],"sourceHash":"tvLW9vOWXjtz4ddhwqNTXALv1ZWESsk-lNR-k1PmqxY","targetHash":"39VRDGVP-F0GkXvqC9gkG8DgSrdIL2beH06ImTU2SiM","targetVersionId":2,"proof":{"type":"DataIntegrityProof","cryptosuite":"bip340-jcs-2025","verificationMethod":"did:btcr2:x1qhw2cawpshxnvlkxwx7ux2t7z78mzkz457r4z6m828nz4gplgcjekdgjv6t#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ax1qhw2cawpshxnvlkxwx7ux2t7z78mzkz457r4z6m828nz4gplgcjekdgjv6t","capabilityAction":"Write","proofValue":"z51BCkYa8kQmSsvz7di6pS3uEVRgAumAbUbUDXcgbs73SQmsqYZp5oWyZAhRuwAE67mGWDrQ4qnCa3735s72ubszt"}}],"smtProofs":[{"id":"w1glyRUKIKCm_NukPSz-ZeSAH4Lp6cj2rw-9bkHgUx0","nonce":"psMuBZKQg4gJhKrU1Y3PdhGN5yMRpTQprXywNkjRB_U","updateId":"dzb0PW1sd8q2rjMLZTHllAyMX8pIMLJ6fhtvwYiCKjc","collapsed":"__________________________________________4","hashes":["PL7ZfaAQu_H72cCW88FTWnzHrXnTQlYMvz_Oy_4aoLE"]}]}}'
```

**Example 12b:** Genesis document-based (from Sidecar) did:btcr2 on Mutinynet with 1 Update (from Sidecar) using SMT Proof (from Sidecar):

*Note: This example uses the same block, transaction, and Sparse Merkle Tree as Example 12a.*

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1q4r57symra5lul4tl0vfenkylytg4awr4u5d8mu2sj5qwqft27ryquqdgkc' -d '{"sidecar":{"genesisDocument":{"verificationMethod":[{"type":"Multikey","id":"#initialKey","publicKeyMultibase":"zQ3shcYqStwqdb2W5n2pytaHecFrhn3MdfPwTvd3Am8reWL19","controller":"did:btcr2:_"}],"assertionMethod":["#initialKey"],"capabilityDelegation":["#initialKey"],"capabilityInvocation":["#initialKey"],"authentication":["#initialKey"],"id":"did:btcr2:_","@context":["https://www.w3.org/ns/did/v1.1","https://btcr2.dev/context/v1"],"service":[{"type":"SingletonBeacon","id":"#initialP2PKH","serviceEndpoint":"bitcoin:mjWJMTk41SqgM5YWsxoUuTCdRo6VLsLaKZ"},{"type":"SingletonBeacon","id":"#initialP2WPKH","serviceEndpoint":"bitcoin:tb1q90qdf42s80mptgg2mcpcxz3yu5thaun3uctcj9"},{"type":"SingletonBeacon","id":"#initialP2TR","serviceEndpoint":"bitcoin:tb1p5upuau5sd3yz3ak7xkc2pwtahy02xdhdx7rf49ezfcz4k6v3v7rqm2jysx"},{"type":"SMTBeacon","id":"#cohort-mutinynet-smt-2","serviceEndpoint":"bitcoin:tb1phaymjanje6hhwusxpcuq9jm8zdavd2j074e5mhmdh3a2vjkzknjq76kg7c"}]},"updates":[{"@context":["https://btcr2.dev/context/v1","https://w3id.org/json-ld-patch/v1","https://w3id.org/zcap/v1","https://w3id.org/security/data-integrity/v2"],"patch":[{"op":"add","path":"/service/4","value":{"id":"#dwn","type":"DecentralizedWebNode","serviceEndpoint":"http://example.com/dwn"}}],"sourceHash":"c98C0DCGQzHXgs7o2FgoRIEtrZnnxb_51WYKHiotICo","targetHash":"eGyhM5CfiQmkl-euAGtiyagC5WRBZRV3WxYTkaJqrZo","targetVersionId":2,"proof":{"type":"DataIntegrityProof","cryptosuite":"bip340-jcs-2025","verificationMethod":"did:btcr2:x1q4r57symra5lul4tl0vfenkylytg4awr4u5d8mu2sj5qwqft27ryquqdgkc#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ax1q4r57symra5lul4tl0vfenkylytg4awr4u5d8mu2sj5qwqft27ryquqdgkc","capabilityAction":"Write","proofValue":"z5MELFSpSnXFcezFaD28HHyF9UaTCsNKkWsmdwy28DgAy3AN2DyumtvVKZntqMXWFYe2ZLcZovau41wvTSmgehMBK"}}],"smtProofs":[{"id":"w1glyRUKIKCm_NukPSz-ZeSAH4Lp6cj2rw-9bkHgUx0","nonce":"7Jtl9-UM68SWplc2wnuFlIKg6DD1SYp8a9njnUy713M","updateId":"0QimzBN_OVXasBh3K-ndM60zg1L4OCdY0tFMubb5s28","collapsed":"__________________________________________4","hashes":["mxRBtSe39FZRTvJemS0IPYm41Z0lI3_7v1mM4LiZ8h8"]}]}}'
```

## Example VCs

*Note: Examples Verifiable Credentials and other JSON-LD documents are available under [./example-vcs/](./example-vcs/).*

## Example ATproto

*Note: Examples ATproto data is available under [./example-atproto/](./example-atproto/).*

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
