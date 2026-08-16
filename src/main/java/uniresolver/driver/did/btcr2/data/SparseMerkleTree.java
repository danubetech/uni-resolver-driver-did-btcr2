package uniresolver.driver.did.btcr2.data;

import uniresolver.driver.did.btcr2.util.SHA256Util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SparseMerkleTree {

    public static final int TREE_DEPTH = 256;
    private static final BigInteger TREE_SIZE = BigInteger.ONE.shiftLeft(TREE_DEPTH);

    private static final byte[] EMPTY_HASH = new byte[32];
    private static final byte[][] ZERO_CACHE = new byte[TREE_DEPTH][];

    static {
        byte[] hash = EMPTY_HASH;
        for  (int i=TREE_DEPTH-1; i>=0; i--) {
            hash = SHA256Util.sha256(concat(hash, hash));
            ZERO_CACHE[i] = hash;
        }
    }

    private static record Leaf(byte[] nonce, byte[] updateData) { }

    private final TreeMap<BigInteger, Leaf> leaves = new TreeMap<>();

    // -------------------------------------------------------------------------
    // Public API – mutations
    // -------------------------------------------------------------------------

    public void insertUpdate(byte[] didIndex, byte[] nonce, byte[] updateData) {
        leaves.put(new BigInteger(1, didIndex), new Leaf(nonce, updateData));
    }

    public void insertUpdate(byte[] didIndex, byte[] updateHash) {
        this.insertUpdate(didIndex, null, updateHash);
    }

    public void insertUpdate(String did, byte[] nonce, byte[] updateData) {
        this.insertUpdate(didToIndex(did), nonce, updateData);
    }

    public void insertUpdate(String did, byte[] updateHash) {
        this.insertUpdate(didToIndex(did), updateHash);
    }

    public void insertNonUpdate(byte[] didIndex, byte[] nonce) {
        leaves.put(new BigInteger(1, didIndex), new Leaf(nonce, EMPTY_HASH));
    }

    public void insertNonUpdate(String did, byte[] nonce) {
        this.insertNonUpdate(didToIndex(did), nonce);
    }

    // -------------------------------------------------------------------------
    // Public API – queries
    // -----------------    --------------------------------------------------------

    public byte[] rootHash() {
        System.out.println("ROOTHASH");
        return subtreeHash(BigInteger.ZERO, TREE_SIZE, leaves, -1).subtreeHash();
    }

    public SMTProof generateProof(byte[] didIndex) {
        return this.generateProof(new BigInteger(1, didIndex));
    }

    public SMTProof generateProof(String did) {
        return this.generateProof(didToIndex(did));
    }

    private SMTProof generateProof(BigInteger didIndex) {
        BigInteger collapsed = new BigInteger(1, EMPTY_HASH);
        List<byte[]> siblingHashes = new ArrayList<>();

        // We walk upward using half-open ranges [lo, hi) to avoid ever computing 2^256.
        BigInteger lo = didIndex;                      // [index, index+1) — leaf range
        BigInteger hi = didIndex.add(BigInteger.ONE);

        for (int level=TREE_DEPTH-1; level>=0; level--) {
            BigInteger range  = hi.subtract(lo);        // 2^level at this step

            // LSB-first: bit `level` of the index is 1 iff we are the RIGHT child.
            // Right child's range starts at parentLo + range; left child starts at parentLo.
            BigInteger parentLo = lo.clearBit(TREE_DEPTH-level-1);
            BigInteger parentRange = range.shiftLeft(1); // 2^(level+1)
            BigInteger parentHi = parentLo.add(parentRange);

            BigInteger siblingLo, siblingHi;
            String leftright;
            if (didIndex.testBit(TREE_DEPTH-level-1)) {
                // We are the RIGHT child; sibling is the LEFT half [parentLo, lo)
                leftright = "RI";
                siblingLo = parentLo;
                siblingHi = lo;
            } else {
                // We are the LEFT child; sibling is the RIGHT half [hi, parentLo+parentRange)
                leftright = "LE";
                siblingLo = hi;
                siblingHi = parentHi;
            }

            System.out.println("LEVEL: " + leftright + " " + level + " " + siblingHi.subtract(siblingLo));
            SubtreeHash subtreeHash = subtreeHash(siblingLo, siblingHi, leaves, level);
            byte[] siblingHash = subtreeHash.subtreeHash();
            boolean siblingCollapsed = subtreeHash.collapsed();

            if (siblingCollapsed) {
                collapsed = collapsed.setBit(level);
            } else {
                siblingHashes.add(siblingHash);
            }

            // Move up to the parent range
            lo = parentLo;
            hi = parentHi;
        }

        byte[] root = rootHash();
        Leaf leaf = leaves.get(didIndex);
        if (leaf == null) throw new IllegalArgumentException("DID not found in SMT.");

        return new SMTProof(root, leaf.nonce(), leaf.updateData(), stripLeadingZero(collapsed.toByteArray()), siblingHashes);
    }

    // -------------------------------------------------------------------------
    // Core recursive hash — O(n log n)
    // -------------------------------------------------------------------------

    private record SubtreeHash(byte[] subtreeHash, boolean collapsed) {  }

    static SubtreeHash subtreeHash(BigInteger lo, BigInteger hi, NavigableMap<BigInteger, Leaf> leafMap, int level) {

        // Range query: leaves in [lo, hi)
        NavigableMap<BigInteger, Leaf> sub = leafMap.subMap(lo, true, hi, false);
        BigInteger range = hi.subtract(lo);
        int size = sub.size();

        System.out.println("SUBTREEHASH: " + range + " (" + level + ") " + " -> " + size);

        if (size == 0) {
            // No leaves → empty subtree.
            return new SubtreeHash(ZERO_CACHE[level], true);
        }

        if (BigInteger.ONE.equals(range)) {
            if (size != 1) throw new IllegalStateException();
            return new SubtreeHash(computeLeafHash(sub.values().iterator().next()), false);
        }

        // Two or more leaves diverge somewhere below → must hash left || right.
        BigInteger mid = lo.add(hi).shiftRight(1); // (lo + hi) / 2
        SubtreeHash leftHash  = subtreeHash(lo,  mid, leafMap, level+1);
        SubtreeHash rightHash = subtreeHash(mid, hi,  leafMap, level+1);

        return new SubtreeHash(SHA256Util.sha256(concat(leftHash.subtreeHash(), rightHash.subtreeHash())), false);
    }

    // -------------------------------------------------------------------------
    // Proof verification (static – verifier side)
    // -------------------------------------------------------------------------

    public static boolean verifyProof(String did, SMTProof smtProof) {

        byte[] candidateHash = computeLeafHash(smtProof.getNonce(), smtProof.getUpdateId());

        BigInteger collapsed = new BigInteger(1, smtProof.getCollapsed());
        BigInteger index = new BigInteger(1, didToIndex(did));

        List<byte[]> siblingHashes = new ArrayList<>(smtProof.getHashes());
        for (int level=TREE_DEPTH-1; level>=0; level--) {
            int bit = TREE_DEPTH - 1 - level;
            byte[] siblingHash;
            if (collapsed.testBit(level)) {
                siblingHash = ZERO_CACHE[level];
            } else {
                siblingHash = siblingHashes.removeFirst();
            }
            if (index.testBit(bit)) {
                candidateHash = SHA256Util.sha256(concat(siblingHash, candidateHash));
            } else {
                candidateHash = SHA256Util.sha256(concat(candidateHash, siblingHash));
            }
        }

        return Arrays.equals(candidateHash, smtProof.getId());
    }

    // -------------------------------------------------------------------------
    // Index derivation
    // -------------------------------------------------------------------------

    public static byte[] didToIndex(String did) {
        return SHA256Util.sha256(did.getBytes(StandardCharsets.UTF_8));
    }

    // -------------------------------------------------------------------------
    // Leaf hash computation
    // -------------------------------------------------------------------------

    private static byte[] computeLeafHash(byte[] nonce, byte[] updateHash) {
        return updateHash != null ? computeLeafForUpdate(nonce, updateHash) : computeLeafForNonUpdate(nonce);
    }

    private static byte[] computeLeafHash(Leaf leaf) {
        return computeLeafHash(leaf.nonce(), leaf.updateData());
    }

    private static byte[] computeLeafForUpdate(byte[] nonce, byte[] updateHash) {
        return nonce == null ? updateHash : SHA256Util.sha256(concat(SHA256Util.sha256(nonce), updateHash));
    }

    private static byte[] computeLeafForNonUpdate(byte[] nonce) {
        return nonce == null ? EMPTY_HASH : SHA256Util.sha256(SHA256Util.sha256(nonce));
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
        return hash == null || hash.length == 0 || Arrays.equals(EMPTY_HASH, hash);
    }

    private static byte[] stripLeadingZero(byte[] bytes) {
        if (bytes == null) return null;
        if (bytes.length == 33 && bytes[0] == 0) {
            return Arrays.copyOfRange(bytes, 1, bytes.length);
        } else {
            return bytes;
        }
    }
}