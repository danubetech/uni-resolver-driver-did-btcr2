package uniresolver.driver.did.btcr2.algorithms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btcr2.data.SMTProof;
import uniresolver.driver.did.btcr2.data.SparseMerkleTree;

import java.util.Arrays;
import java.util.Base64;

/*
 * SMT Proof Verification
 * See https://dcdpr.github.io/did-btcr2/algorithms.html#smt-proof-verification
 */
public class SMTProofVerification {

    private static final Logger log = LoggerFactory.getLogger(SMTProofVerification.class);

    public static boolean smtProofVerification(String did, SMTProof smtProof, byte[] rootHash) {

        if (! Arrays.equals(smtProof.getId(), rootHash)) {
            log.warn("smtProof.id {} does not match provided rootHash {}", Base64.getUrlEncoder().encode(smtProof.getId()), Base64.getUrlEncoder().encodeToString(rootHash));
            return false;
        }

        return SparseMerkleTree.verifyProof(did, smtProof);
    }
}
