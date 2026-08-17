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
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1q4cytk3ae3y74w2q0k2ukf5hqdjvn7c33ajdu5dwmvk76h3k4sqrqmvw4hp' -d '{"sidecar":{"smtProofs":[{"id":"q1H_iaYG0Oq6gbrycYL-r7FjUsJLnIpHDn49TLeONNA","nonce":"99jndCBWHpZfmObXlIvRGHaPMgoQKXIETdD4H-XqryE","updateId":"njYNViJq2OmhSw1fLfARPCj12RY3VXKGWdS3-7OQ2BE","collapsed":"v_________________________________________8","hashes":["8JWXL7chPKJXwg-i9O1EFTHan_oOO_RmglDpu_ugax0"]}]}}'
```

**Example 11b:** Genesis document-based (from CAS) did:btcr2 on Mutinynet with 1 Update (from CAS) using SMT Proof (from Sidecar):

*Note: This example uses the same block, transaction, and Sparse Merkle Tree as Example 11a.*

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1q4plmrumnc5638xr74w59zg6yavk5laevq8r0jzm0xr7v2v0au655gn9xr0' -d '{"sidecar":{"smtProofs":[{"id":"q1H_iaYG0Oq6gbrycYL-r7FjUsJLnIpHDn49TLeONNA","nonce":"qLA_Nnn4dWnkyMlEAEWooZSa_a698i_enhBVjGgJ1Ts","updateId":"8JWXL7chPKJXwg-i9O1EFTHan_oOO_RmglDpu_ugax0","collapsed":"v_________________________________________8","hashes":["njYNViJq2OmhSw1fLfARPCj12RY3VXKGWdS3-7OQ2BE"]}]}}'
```

**Example 12a:** Genesis document-based (from Sidecar) did:btcr2 on Mutinynet with 1 Update (from Sidecar) using SMT Proof (from Sidecar):

*Note: This example uses the same block, transaction, and Sparse Merkle Tree as Example 12b.*

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1qk8wjcft0ypu2ju5v89p590jaklr30w7tprlpk9rt22f4y2qlq2j7fvyr2z' -d '{"sidecar":{"genesisDocument":{"verificationMethod":[{"type":"Multikey","id":"#initialKey","publicKeyMultibase":"zQ3shYMYeFyoPBrzjERPMFZJT4KDtukvTjLxE3utF7WheeZqK","controller":"did:btcr2:_"}],"assertionMethod":["#initialKey"],"capabilityDelegation":["#initialKey"],"capabilityInvocation":["#initialKey"],"authentication":["#initialKey"],"id":"did:btcr2:_","@context":["https://www.w3.org/ns/did/v1.1","https://btcr2.dev/context/v1"],"service":[{"type":"SingletonBeacon","id":"#initialP2PKH","serviceEndpoint":"bitcoin:mkHtHHQuUusioti869qvhZAn3VKsHmSdTm"},{"type":"SingletonBeacon","id":"#initialP2WPKH","serviceEndpoint":"bitcoin:tb1qx30a7rtxad9pcrvfzt9unnmc9m7we9j40vsyjt"},{"type":"SingletonBeacon","id":"#initialP2TR","serviceEndpoint":"bitcoin:tb1p4mt5e3h5s2kgrhxfx60p7arnhnquq7qha4vf6q6egxnwg2adlxjsuwlu93"},{"type":"SMTBeacon","id":"#cohort-mutinynet-smt-2","serviceEndpoint":"bitcoin:tb1pg5zz46v2fynlzm5rmjce3u4m86x90t9me2tprz4et3w52f3s6vaqntpjhk"}]},"updates":[{"@context":["https://btcr2.dev/context/v1","https://w3id.org/json-ld-patch/v1","https://w3id.org/zcap/v1","https://w3id.org/security/data-integrity/v2"],"patch":[{"op":"add","path":"/service/4","value":{"id":"#didcomm","type":"DIDCommMessaging","serviceEndpoint":"http://example.com/didcomm"}}],"sourceHash":"qv-_v_q4cWmWAqHt3RE0RUCKvtvZ00kLOpGV_2YfYfE","targetHash":"hkABqf5Su7s0qHFX62ovZuSRIcBmWcyxmG13kfkfOuQ","targetVersionId":2,"proof":{"type":"DataIntegrityProof","cryptosuite":"bip340-jcs-2025","verificationMethod":"did:btcr2:x1qk8wjcft0ypu2ju5v89p590jaklr30w7tprlpk9rt22f4y2qlq2j7fvyr2z#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ax1qk8wjcft0ypu2ju5v89p590jaklr30w7tprlpk9rt22f4y2qlq2j7fvyr2z","capabilityAction":"Write","proofValue":"z4tGTTcC13Gn9dm9X2cxdEV5Fu1qaCroftnSoPMqVRCjvpj2h7YXmRvwkpDJ46taGf3pjKPSZhJVPST3MCF8RZGdW"}}],"smtProofs":[{"id":"NLGNqk7ah21UEpBN-azj6WctkS-vnMc80pz_K1xR7nA","updateId":"mhi9IkfKLvSKj_N784Ig-uqVWAD4heT-5x_NDZiiN-U","collapsed":"__________________________________________s","hashes":["ghD4NTQmx31Ydg9ffKKlb5AJFTNZytMt9mi8_jotfRo"]}]}}'
```

**Example 12b:** Genesis document-based (from Sidecar) did:btcr2 on Mutinynet with 1 Update (from Sidecar) using SMT Proof (from Sidecar):

*Note: This example uses the same block, transaction, and Sparse Merkle Tree as Example 12a.*

```
curl -X POST 'http://localhost:8080/1.0/identifiers/did:btcr2:x1qh9vyxphx0rhgpmwpa0p3u0qd5m8kf9lnya2tutsyfsf7z27jt09kargft7' -d '{"sidecar":{"genesisDocument":{"verificationMethod":[{"type":"Multikey","id":"#initialKey","publicKeyMultibase":"zQ3shbNV1j4QtPjDjiBsS5cwt7PdzeC4qPF39Rk2UzR65JsSD","controller":"did:btcr2:_"}],"assertionMethod":["#initialKey"],"capabilityDelegation":["#initialKey"],"capabilityInvocation":["#initialKey"],"authentication":["#initialKey"],"id":"did:btcr2:_","@context":["https://www.w3.org/ns/did/v1.1","https://btcr2.dev/context/v1"],"service":[{"type":"SingletonBeacon","id":"#initialP2PKH","serviceEndpoint":"bitcoin:mnQmQgxooxLqcW88KMLCDwar7vh4CF1Xdt"},{"type":"SingletonBeacon","id":"#initialP2WPKH","serviceEndpoint":"bitcoin:tb1qfww5csssvmjt6hfvc47z3y4uwuq9l7qa3ju7vk"},{"type":"SingletonBeacon","id":"#initialP2TR","serviceEndpoint":"bitcoin:tb1pfv5lm7p4syv5r8d2zusd6rhl4t6jd8marp3hxvlht9mykn7g5zystfj23y"},{"type":"SMTBeacon","id":"#cohort-mutinynet-smt-2","serviceEndpoint":"bitcoin:tb1pg5zz46v2fynlzm5rmjce3u4m86x90t9me2tprz4et3w52f3s6vaqntpjhk"}]},"updates":[{"@context":["https://btcr2.dev/context/v1","https://w3id.org/json-ld-patch/v1","https://w3id.org/zcap/v1","https://w3id.org/security/data-integrity/v2"],"patch":[{"op":"add","path":"/service/4","value":{"id":"#dwn","type":"DecentralizedWebNode","serviceEndpoint":"http://example.com/dwn"}}],"sourceHash":"pBl8kB1h3r9UQVxPtmTUVIR9hsGGGGfsmudHOflHQOg","targetHash":"0JcUU-IGsZ6V9Drj1Q46TUXqi3kSJNmZaNAhdY7dioA","targetVersionId":2,"proof":{"type":"DataIntegrityProof","cryptosuite":"bip340-jcs-2025","verificationMethod":"did:btcr2:x1qh9vyxphx0rhgpmwpa0p3u0qd5m8kf9lnya2tutsyfsf7z27jt09kargft7#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ax1qh9vyxphx0rhgpmwpa0p3u0qd5m8kf9lnya2tutsyfsf7z27jt09kargft7","capabilityAction":"Write","proofValue":"z49z7qGP54Zgs2Qt2BayvrfVCAm5dMZaBrd7oKUmgiwaH2qn643HcKhuh82VuLNeewJX8Qkt3myPzSVo81vmcqgw7"}}],"smtProofs":[{"id":"NLGNqk7ah21UEpBN-azj6WctkS-vnMc80pz_K1xR7nA","updateId":"EU2hsGpGXXkjDk0HyByp_CzT6PD7iwF7L6TznFYJ0xI","collapsed":"__________________________________________s","hashes":["zWPIP599tSweWJ01Zk_zzDw8GD_eAZVbeFhYporMzns"]}]}}'
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
