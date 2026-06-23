package uniresolver.driver.did.btcr2.data;

import uniresolver.driver.did.btcr2.util.SHA256Util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

public class SparseMerkleTree {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    public static final int TREE_DEPTH = 256;
    public static final int NONCE_SIZE = 256/8;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final BigInteger FULL_SIZE = BigInteger.ONE.shiftLeft(TREE_DEPTH);
    static final byte[][] CACHED_ZERO = buildCachedZeros();

    private static byte[][] buildCachedZeros() {
        byte[][] cz = new byte[TREE_DEPTH + 1][];
        byte[] z =  new byte[32];
        for (int i = 0; i <= TREE_DEPTH; i++) {
            z = SHA256Util.sha256(concat(z, z));
            cz[i] = z;
        }
        return cz;
    }

    /** Sentinel meaning "no leaf value exists at this index" (distinct from a cached zero). */
    static final byte[] EMPTY = new byte[0];

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private final TreeMap<BigInteger, byte[]> leaves = new TreeMap<>();
    private final HashMap<BigInteger, byte[]> nonces = new HashMap<>();
    private final HashMap<BigInteger, byte[]> updateIds = new HashMap<>();

    // -------------------------------------------------------------------------
    // Public API – mutations
    // -------------------------------------------------------------------------

    public void insertNonUpdate(String did, byte[] nonce) {
        BigInteger index = didToIndex(did);
        nonces.put(index, nonce);
        leaves.put(index, computeLeafHash(nonce, null));
    }

    public void insertNonUpdate(String did) {
        byte[] nonce = new byte[NONCE_SIZE];
        SECURE_RANDOM.nextBytes(nonce);
        this.insertNonUpdate(did, nonce);
    }

    public void insertUpdate(String did, byte[] nonce, byte[] updateId) {
        BigInteger index = didToIndex(did);
        nonces.put(index, nonce);
        updateIds.put(index, updateId);
        leaves.put(index, computeLeafHash(nonce, updateId));
    }

    public void insertUpdate(String did, byte[] updateId) {
        byte[] nonce = new byte[NONCE_SIZE];
        SECURE_RANDOM.nextBytes(nonce);
        this.insertUpdate(did, nonce, updateId);
    }

    // -------------------------------------------------------------------------
    // Public API – queries
    // -------------------------------------------------------------------------

    public byte[] rootHash() {
        return subtreeHash(BigInteger.ZERO, FULL_SIZE, TREE_DEPTH, leaves);
    }

    public SMTProof generateProof(String did) {
        return generateProofForIndex(didToIndex(did));
    }

    public SMTProof generateProofForIndex(BigInteger index) {
        byte[] root = rootHash();
        List<byte[]> siblingHashes = new ArrayList<>();
        BigInteger collapsed = BigInteger.ZERO;

        // Walk upward using half-open ranges [lo, hi).
        // At level 0: [index, index+1) — the leaf itself.
        // At level k: the subtree containing our leaf, of height k (covering 2^k leaves).
        BigInteger lo = index;
        BigInteger hi = index.add(BigInteger.ONE);

        for (int level = 0; level < TREE_DEPTH; level++) {
            // LSB-first: index.testBit(level) == 1 means we are the RIGHT child at this level.
            BigInteger parentLo   = lo.clearBit(level);
            BigInteger parentSize = BigInteger.ONE.shiftLeft(level + 1);

            BigInteger sibLo, sibHi;
            if (index.testBit(level)) {
                // We are the RIGHT child; sibling is the LEFT half [parentLo, lo).
                sibLo = parentLo;
                sibHi = lo;
            } else {
                // We are the LEFT child; sibling is the RIGHT half [hi, parentLo+parentSize).
                sibLo = hi;
                sibHi = parentLo.add(parentSize);
            }

            // O(log n) range query: does the sibling subtree have any leaves?
            if (leaves.subMap(sibLo, true, sibHi, false).isEmpty()) {
                // Sibling is entirely empty → verifier substitutes CACHED_ZERO[level].
                collapsed = collapsed.setBit(level);
            } else {
                // Sibling has at least one leaf → compute its hash and include in the proof.
                // The sibling subtree has height `level` (covers 2^level leaves).
                siblingHashes.add(subtreeHash(sibLo, sibHi, level, leaves));
            }

            lo = parentLo;
            hi = parentLo.add(parentSize);
        }

        return new SMTProof(root, nonces.get(index), updateIds.get(index), collapsed, siblingHashes);
    }

    public SMTProof generateProofForIndex(byte[] index) {
        return this.generateProofForIndex(new BigInteger(1, index));
    }

    // -------------------------------------------------------------------------
    // Proof verification (static – verifier side)
    // -------------------------------------------------------------------------

    public static boolean verifyProof(SMTProof proof, byte[] rootHash, String did) {
        return verifyProofWithDepth(proof, rootHash, did, TREE_DEPTH);
    }

    static boolean verifyProofWithDepth(SMTProof smtProof, byte[] rootHash, String did, int treeDepth) {
        if (smtProof.getUpdateId() == null || isEmpty(smtProof.getUpdateId())) return false;

        byte[]     nonce         = smtProof.getNonce();
        BigInteger index         = didToIndex(did);
        BigInteger collapsed     = new BigInteger(1, smtProof.getCollapsed());
        byte[]     updateId      = smtProof.getUpdateId();

        byte[]     nonceHash     = SHA256Util.sha256(nonce);
        byte[]     candidateHash = (updateId == null) ? SHA256Util.sha256(nonceHash) : SHA256Util.sha256(concat(nonceHash, updateId));
        int        hashPtr       = 0;

        for (int level = 0; level < treeDepth; level++) {
            // Determine the sibling hash: either from the proof or a cached zero.
            final byte[] sibHash;
            if (collapsed.testBit(level)) {
                // Sibling subtree is empty at this height; use the precomputed cached zero.
                sibHash = CACHED_ZERO[level];
            } else {
                if (hashPtr >= smtProof.getHashes().size()) return false;
                sibHash = smtProof.getHashes().get(hashPtr++);
            }

            // LSB-first: index bit `level` == 1 means candidate is the RIGHT child.
            // Hash is always performed, even for collapsed (empty-sibling) levels.
            boolean isRight = index.testBit(level);
            candidateHash = isRight
                    ? SHA256Util.sha256(concat(sibHash, candidateHash))
                    : SHA256Util.sha256(concat(candidateHash, sibHash));
        }

        return Arrays.equals(candidateHash, rootHash);
    }

    // -------------------------------------------------------------------------
    // Index derivation
    // -------------------------------------------------------------------------

    public static BigInteger didToIndex(String did) {
        return new BigInteger(1, SHA256Util.sha256(did.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    // -------------------------------------------------------------------------
    // Leaf hash computation
    // -------------------------------------------------------------------------

    public static byte[] computeLeafHash(byte[] nonce, byte[] updateId) {
        if (updateId != null) {
            if (nonce != null) {
                return SHA256Util.sha256(concat(SHA256Util.sha256(nonce), updateId));
            } else {
                return updateId;
            }
        } else {
            if (nonce != null) {
                return SHA256Util.sha256(SHA256Util.sha256(nonce));
            } else {
                return new byte[0];
            }
        }
    }

    // -------------------------------------------------------------------------
    // Core recursive hash — O(n log n)
    // -------------------------------------------------------------------------

    static byte[] subtreeHash(BigInteger lo, BigInteger hi, int height,
                              NavigableMap<BigInteger, byte[]> leafMap) {
        NavigableMap<BigInteger, byte[]> sub = leafMap.subMap(lo, true, hi, false);

        switch (sub.size()) {
            case 0:
                // Entire subtree is empty: return the precomputed canonical zero for this height.
                return CACHED_ZERO[height];

            case 1:
                // Exactly one leaf: hash it upward through all `height` levels, filling each
                // empty sibling slot with the appropriate CACHED_ZERO[l].
                Map.Entry<BigInteger, byte[]> entry = sub.firstEntry();
                return hashLeafToSubtreeRoot(entry.getKey(), entry.getValue(), height);

            default:
                // Two or more leaves: they must diverge somewhere in [lo, hi).
                // Recurse into left and right halves, then hash the results together.
                BigInteger mid = lo.add(hi).shiftRight(1);
                byte[] leftHash  = subtreeHash(lo,  mid, height - 1, leafMap);
                byte[] rightHash = subtreeHash(mid, hi,  height - 1, leafMap);
                // Both sides are guaranteed non-empty (each holds ≥1 leaf after splitting
                // ≥2 leaves). No need for empty guards — but kept for defensive correctness.
                return SHA256Util.sha256(concat(leftHash, rightHash));
        }
    }

    static byte[] hashLeafToSubtreeRoot(BigInteger leafIndex, byte[] leafHash, int height) {
        byte[] result = leafHash;
        for (int l = 0; l < height; l++) {
            if (leafIndex.testBit(l)) {
                // Leaf is the RIGHT child at level l; empty sibling goes on the left.
                result = SHA256Util.sha256(concat(CACHED_ZERO[l], result));
            } else {
                // Leaf is the LEFT child at level l; empty sibling goes on the right.
                result = SHA256Util.sha256(concat(result, CACHED_ZERO[l]));
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Cryptographic primitives
    // -------------------------------------------------------------------------

    static byte[] concat(byte[] a, byte[] b) {
        byte[] r = new byte[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    static boolean isEmpty(byte[] hash) {
        return hash == null || hash.length == 0;
    }
}