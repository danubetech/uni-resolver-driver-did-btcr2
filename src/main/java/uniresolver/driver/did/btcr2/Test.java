package uniresolver.driver.did.btcr2;

import uniresolver.driver.did.btcr2.algorithms.SMTProofVerification;
import uniresolver.driver.did.btcr2.data.SMTProof;
import uniresolver.driver.did.btcr2.data.SparseMerkleTree;
import uniresolver.driver.did.btcr2.util.SHA256Util;

import java.io.StringReader;
import java.util.Base64;
import java.util.Random;

public class Test {

    public static void main(String[] args) throws Exception {

        // from bryan
        System.out.println(SMTProofVerification.smtProofVerification(
                "did:btcr2:x1q5cfewepdtyw92pylcgf68k87dz97epx3a2040gayx3jygj2g45fs4peu2c",
                SMTProof.fromJson(new StringReader(
                """
                        {
                               "id": "eZs-zcWMKf9OIGQ3zrjgoZS2o8zdjR1UpYfAzxp8cJ8",
                               "collapsed": "__________________________________________4",
                               "hashes": [
                                 "en-HJuukd-2G8ebKQMew2lmKA-UW5ojb2t0rXQ0fRx4"
                               ],
                               "nonce": "hbpVJCDARc5Tr5iFGEvvWMrvULtLOBbflr3G6-WNjfg",
                               "updateId": "BHudVMXdO29QuM4ZOIgvmKx3mlHRB3Dp9hG9zoH3o98"
                       }
                   """
        )),
        Base64.getUrlDecoder().decode("eZs-zcWMKf9OIGQ3zrjgoZS2o8zdjR1UpYfAzxp8cJ8")));

        SparseMerkleTree sparseMerkleTree = new SparseMerkleTree();
        String did1 = "did:ex:12a3", did2 = "did:ex:s5efccwqer6", did3 = "did:ex:789";
        byte[] nonce1 = new byte[32], nonce2 = new byte[32], nonce3 = new byte[32];
        new Random().nextBytes(nonce1); new Random().nextBytes(nonce2); new Random().nextBytes(nonce3);
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(nonce1));
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(nonce2));
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(nonce3));
        byte[] updateId1 = SHA256Util.sha256(new byte[] { 1, 2, 3 });
        byte[] updateId2 = SHA256Util.sha256(new byte[] { 4, 5, 6 });
        byte[] updateId3 = SHA256Util.sha256(new byte[] { 7, 8, 9 });
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(updateId1));
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(updateId2));
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(updateId3));
        sparseMerkleTree.insertUpdate(did1, nonce1, updateId1);
        //sparseMerkleTree.insertUpdate(did2, nonce2, updateId2);
        //sparseMerkleTree.insertUpdate(did3, nonce3, updateId3);
        SMTProof smtProof1 = sparseMerkleTree.generateProof(did1);
        //SMTProof smtProof2 = sparseMerkleTree.generateProof(did2);
        //SMTProof smtProof3 = sparseMerkleTree.generateProof(did3);
        System.out.println(smtProof1);
        //System.out.println(smtProof2);
        //System.out.println(smtProof3);
        System.out.println(SMTProofVerification.smtProofVerification(did1, smtProof1, smtProof1.getId()));
        //System.out.println(SMTProofVerification.smtProofVerification(did2, smtProof2, smtProof2.getId()));
        //System.out.println(SMTProofVerification.smtProofVerification(did3, smtProof3, smtProof3.getId()));
    }
}
