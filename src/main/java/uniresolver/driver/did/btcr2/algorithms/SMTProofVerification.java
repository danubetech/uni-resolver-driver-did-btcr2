package uniresolver.driver.did.btcr2.algorithms;

import uniresolver.driver.did.btcr2.data.SMTProof;
import uniresolver.driver.did.btcr2.data.SparseMerkleTree;

/*
 * SMT Proof Verification
 * See https://dcdpr.github.io/did-btcr2/algorithms.html#smt-proof-verification
 */
public class SMTProofVerification {

    public static boolean smtProofVerification(SMTProof smtProof, byte[] rootHash, String did) {
        return SparseMerkleTree.verifyProof(smtProof, rootHash, did);
    }
}
