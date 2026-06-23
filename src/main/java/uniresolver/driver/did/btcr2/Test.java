package uniresolver.driver.did.btcr2;

import com.danubetech.dataintegrity.DataIntegrityProof;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDURL;
import uniresolver.driver.did.btcr2.algorithms.JSONDocumentHashing;
import uniresolver.driver.did.btcr2.algorithms.SMTProofVerification;
import uniresolver.driver.did.btcr2.data.SMTProof;
import uniresolver.driver.did.btcr2.data.SparseMerkleTree;
import uniresolver.driver.did.btcr2.data.jsonld.BTCR2Update;
import uniresolver.driver.did.btcr2.util.SHA256Util;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Test {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String MY12A_UPDATE = """
        {"@context":["https://btcr2.dev/context/v1","https://w3id.org/json-ld-patch/v1","https://w3id.org/zcap/v1","https://w3id.org/security/data-integrity/v2"],"patch":[{"op":"add","path":"/service/4","value":{"id":"#didcomm","type":"DIDCommMessaging","serviceEndpoint":"http://example.com/didcomm"}}],"sourceHash":"oPS5ZrqJt0dLLK73-_jlWZFeE_vPFpUEip-loHcoIOc","targetHash":"SGqaVFjpiKvkgFfPF2Xvtu0KupByjRYV3jtViAGhJ9o","targetVersionId":2,"proof":{"type":"DataIntegrityProof","cryptosuite":"bip340-jcs-2025","verificationMethod":"did:btcr2:x1q5h2tzafcundemvxuaxl9y0z922cwv8suev6c3un9xyvc3yuyxmjz3yadsd#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ax1q5h2tzafcundemvxuaxl9y0z922cwv8suev6c3un9xyvc3yuyxmjz3yadsd","capabilityAction":"Write","proofValue":"z4435aBTPk8tytyWSugtsTJnKowW1uvx1UGf3g53QKaRoJ78bAcvA3jkeLqkN9NWmh1rqmrZbWyZhxv4Nhzi55WGX"}}
        """;

    private static final String MY12A_SMTPROOF = """
            {
              "id": "Zeuswi8sMNygdfuKjh9YGaUQOK4zPcirIJRwzIA7ZGU",
              "nonce": "AODC1L3cjDCo8zqlORdM8jDSG0ddu_XnblSLA7KHf00K",
              "updateId": "QbPkfJIHH21IeMRyGiNg5NfKYt0TmCljg91evyo-MpU",
              "collapsed": "f_________________________________________8",
              "hashes": [
                "SAGvc3PNM_JeqGZ8QG2aJdExqHdvUnYL8UkIPm18a9I"
              ]
            }
        """;

    private static final String MY12B_UPDATE = """
        {"@context":["https://btcr2.dev/context/v1","https://w3id.org/json-ld-patch/v1","https://w3id.org/zcap/v1","https://w3id.org/security/data-integrity/v2"],"patch":[{"op":"add","path":"/service/4","value":{"id":"#dwn","type":"DecentralizedWebNode","serviceEndpoint":"http://example.com/dwn"}}],"sourceHash":"Ilmr4EGhB-eM0K2OHTrOwlkqAECfpnvxLCltMYIiNic","targetHash":"2RyLIfzaJ3YQ9KX3OGUdCTTInWDfUj2ooqXWbFIuqaI","targetVersionId":2,"proof":{"type":"DataIntegrityProof","cryptosuite":"bip340-jcs-2025","verificationMethod":"did:btcr2:x1qhqkzp82h5266cyup4mthjfyrv27yasr7kn7l47escnplmw3vamyww8jqy8#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ax1qhqkzp82h5266cyup4mthjfyrv27yasr7kn7l47escnplmw3vamyww8jqy8","capabilityAction":"Write","proofValue":"z4cy7TZFu9cKR9mDHmHVMwNm4Xc8wVzmwRop8XZBswjquHRJtwuiTBHphvQwxkf1uZk9mbEUyArrJ8Bo1SS2qqwum"}}
        """;

    private static final String MY12B_SMTPROOF = """
            {
              "id": "Zeuswi8sMNygdfuKjh9YGaUQOK4zPcirIJRwzIA7ZGU",
              "nonce": "HZ6T_0Hrj463dlEhMPSJRzaZnFhOnNe0L-NFCeEidPk",
              "updateId": "SAGvc3PNM_JeqGZ8QG2aJdExqHdvUnYL8UkIPm18a9I",
              "collapsed": "f_________________________________________8",
              "hashes": [
                "QbPkfJIHH21IeMRyGiNg5NfKYt0TmCljg91evyo-MpU"
              ]
            }
        """;

    private static final String OTHER12A_UPDATE = """
        {"@context":["https://w3id.org/security/v2","https://w3id.org/zcap/v1","https://w3id.org/json-ld-patch/v1","https://btcr2.dev/context/v1"],"patch":[{"op":"add","path":"/service/4","value":{"id":"#didcomm","type":"DIDCommMessaging","serviceEndpoint":"http://example.com/didcomm"}}],"targetHash":"7rKT9FXzOuulrenbULbMZdukk4IMQyK9A-YargjkBLI","targetVersionId":2,"sourceHash":"zQhcSQNQ_efhcrXsOERI7aY6p6KMy_I35dIobYjPgvQ","proof":{"@context":["https://w3id.org/security/v2","https://w3id.org/zcap/v1","https://w3id.org/json-ld-patch/v1","https://btcr2.dev/context/v1"],"cryptosuite":"bip340-jcs-2025","type":"DataIntegrityProof","verificationMethod":"did:btcr2:x1q5cfewepdtyw92pylcgf68k87dz97epx3a2040gayx3jygj2g45fs4peu2c#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ax1q5cfewepdtyw92pylcgf68k87dz97epx3a2040gayx3jygj2g45fs4peu2c","capabilityAction":"Write","proofValue":"z2dDZ9652EWpwXrr5Xr2aNG26pmqs9pWPamUvyMr7Pjv3pJXVk3HXQfv2Xo9DeGARXuvichHysE8rNPjyzXRFxEj6"}}
        """;

    private static final String OTHER12A_SMTPROOF = """
            {
               "id": "eZs-zcWMKf9OIGQ3zrjgoZS2o8zdjR1UpYfAzxp8cJ8",
               "collapsed": "__________________________________________4",
               "hashes": [
                 "en-HJuukd-2G8ebKQMew2lmKA-UW5ojb2t0rXQ0fRx4"
               ],
               "nonce": "hbpVJCDARc5Tr5iFGEvvWMrvULtLOBbflr3G6-WNjfg",
               "updateId": "BHudVMXdO29QuM4ZOIgvmKx3mlHRB3Dp9hG9zoH3o98"
             }
        """;

    private static final String OTHER12B_UPDATE = """
        {"@context":["https://w3id.org/security/v2","https://w3id.org/zcap/v1","https://w3id.org/json-ld-patch/v1","https://btcr2.dev/context/v1"],"patch":[{"op":"add","path":"/service/4","value":{"id":"#dwn","type":"DecentralizedWebNode","serviceEndpoint":"http://example.com/dwn"}}],"targetHash":"1czgUaJlcOiRAVx9q_bJ6NRp3J5jqNbyVmvMbfQaDNI","targetVersionId":2,"sourceHash":"XrNOMcrIeHPAivwGVCDs9vgNzCWj_VH5BW5na1UDa8E","proof":{"@context":["https://w3id.org/security/v2","https://w3id.org/zcap/v1","https://w3id.org/json-ld-patch/v1","https://btcr2.dev/context/v1"],"cryptosuite":"bip340-jcs-2025","type":"DataIntegrityProof","verificationMethod":"did:btcr2:x1q425c5wf9a7r3hf4vm6rtxlz4rpapwgcqt9jht8v33mnehp6cs3qku7xtk2#initialKey","proofPurpose":"capabilityInvocation","capability":"urn:zcap:root:did%3Abtcr2%3Ax1q425c5wf9a7r3hf4vm6rtxlz4rpapwgcqt9jht8v33mnehp6cs3qku7xtk2","capabilityAction":"Write","proofValue":"z3gk6HRVj1STyPnx3yQNQWx84pEn1e5e2NUAgLEF5JTciGH85qzsfRGyoqYdv4H1Yem2V1uaJJKt9BmY3aT3o9BnT"}}
        """;

    private static final String OTHER12B_SMTPROOF = """
            {
              "id": "eZs-zcWMKf9OIGQ3zrjgoZS2o8zdjR1UpYfAzxp8cJ8",
              "collapsed": "__________________________________________4",
              "hashes": [
                "eO_ES8yVFpGewJV5Gel_Iyim7iMbgqqigc8HYaLss8M"
              ],
              "nonce": "MgeYIFvBNhyZaSddpnTDYUCgZqI5-skKF0vdvYUcnok",
              "updateId": "WjrUtSnXjs-XH9VrbRMTuj7NuG9tXbe6_SoginFncKI"
            }
        """;

    public static void main(String[] args) throws Exception {

        BTCR2Update btcr2Update_MY12A = BTCR2Update.fromJson(MY12A_UPDATE);
        String did_MY12A = DIDURL.fromUri(DataIntegrityProof.getFromJsonLDObject(btcr2Update_MY12A).getVerificationMethod()).getDid().toString();
        byte[] updateId_MY12A = JSONDocumentHashing.jsonDocumentHashing(btcr2Update_MY12A);
        SMTProof smtProof_MY12A = SMTProof.fromJson(new StringReader(MY12A_SMTPROOF));
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(updateId_MY12A) + " --> " + SMTProofVerification.smtProofVerification(smtProof_MY12A, smtProof_MY12A.getId(), did_MY12A));

        BTCR2Update btcr2Update_MY12B = BTCR2Update.fromJson(MY12B_UPDATE);
        String did_MY12B = DIDURL.fromUri(DataIntegrityProof.getFromJsonLDObject(btcr2Update_MY12B).getVerificationMethod()).getDid().toString();
        byte[] updateId_MY12B = JSONDocumentHashing.jsonDocumentHashing(btcr2Update_MY12B);
        SMTProof smtProof_MY12B = SMTProof.fromJson(new StringReader(MY12B_SMTPROOF));
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(updateId_MY12B) + " --> " + SMTProofVerification.smtProofVerification(smtProof_MY12B, smtProof_MY12B.getId(), did_MY12B));

        SparseMerkleTree sparseMerkleTree_MY =  new SparseMerkleTree();
        sparseMerkleTree_MY.insertUpdate(did_MY12A, smtProof_MY12A.getNonce(), updateId_MY12A);
        sparseMerkleTree_MY.insertUpdate(did_MY12B, smtProof_MY12B.getNonce(), updateId_MY12B);
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(sparseMerkleTree_MY.rootHash()) + " == " + Base64.getUrlEncoder().withoutPadding().encodeToString(smtProof_MY12A.getId()) + " == " + Base64.getUrlEncoder().withoutPadding().encodeToString(smtProof_MY12B.getId()));
        System.out.println(objectMapper.writeValueAsString(sparseMerkleTree_MY.generateProof(did_MY12A)));
        System.out.println(objectMapper.writeValueAsString(sparseMerkleTree_MY.generateProof(did_MY12B)));



        BTCR2Update btcr2Update_OTHER12A = BTCR2Update.fromJson(OTHER12A_UPDATE);
        String did_OTHER12A = DIDURL.fromUri(DataIntegrityProof.getFromJsonLDObject(btcr2Update_OTHER12A).getVerificationMethod()).getDid().toString();
        byte[] updateId_OTHER12A = JSONDocumentHashing.jsonDocumentHashing(btcr2Update_OTHER12A);
        SMTProof smtProof_OTHER12A = SMTProof.fromJson(new StringReader(OTHER12A_SMTPROOF));
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(updateId_OTHER12A) + " --> " + SMTProofVerification.smtProofVerification(smtProof_OTHER12A, smtProof_OTHER12A.getId(), did_OTHER12A));

        BTCR2Update btcr2Update_OTHER12B = BTCR2Update.fromJson(OTHER12B_UPDATE);
        String did_OTHER12B = DIDURL.fromUri(DataIntegrityProof.getFromJsonLDObject(btcr2Update_OTHER12B).getVerificationMethod()).getDid().toString();
        byte[] updateId_OTHER12B = JSONDocumentHashing.jsonDocumentHashing(btcr2Update_OTHER12B);
        SMTProof smtProof_OTHER12B = SMTProof.fromJson(new StringReader(OTHER12B_SMTPROOF));
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(updateId_OTHER12B) + " --> " + SMTProofVerification.smtProofVerification(smtProof_OTHER12B, smtProof_OTHER12B.getId(), did_OTHER12B));

        SparseMerkleTree sparseMerkleTree_OTHER =  new SparseMerkleTree();
        sparseMerkleTree_OTHER.insertUpdate(did_OTHER12A, smtProof_OTHER12A.getNonce(), updateId_OTHER12A);
        sparseMerkleTree_OTHER.insertUpdate(did_OTHER12B, smtProof_OTHER12B.getNonce(), updateId_OTHER12B);
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(sparseMerkleTree_OTHER.rootHash()) + " == " + Base64.getUrlEncoder().withoutPadding().encodeToString(smtProof_OTHER12A.getId()) + " == " + Base64.getUrlEncoder().withoutPadding().encodeToString(smtProof_OTHER12B.getId()));
        System.out.println(objectMapper.writeValueAsString(sparseMerkleTree_OTHER.generateProof(did_OTHER12A)));
        System.out.println(objectMapper.writeValueAsString(sparseMerkleTree_OTHER.generateProof(did_OTHER12B)));
    }
}
