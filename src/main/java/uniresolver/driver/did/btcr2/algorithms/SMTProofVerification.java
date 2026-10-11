package uniresolver.driver.did.btcr2.algorithms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btcr2.data.SMTProof;
import uniresolver.driver.did.btcr2.data.SparseMerkleTree;
import uniresolver.driver.did.btcr2.util.Base64Util;

import java.util.Arrays;

/*
 * SMT Proof Verification
 * See https://dcdpr.github.io/did-btcr2/algorithms.html#smt-proof-verification
 */
public class SMTProofVerification {

    private static final Logger log = LoggerFactory.getLogger(SMTProofVerification.class);

    public static boolean smtProofVerification(String did, SMTProof smtProof, byte[] rootHash) {

        if (! Arrays.equals(smtProof.getId(), rootHash)) {
            log.warn("smtProof.id {} does not match provided rootHash {}", Base64Util.base64UrlWithoutPaddingEncodeToString(smtProof.getId()), Base64Util.base64UrlWithoutPaddingEncodeToString(rootHash));
            return false;
        }

        return SparseMerkleTree.verifyProof(did, smtProof);
    }
}
