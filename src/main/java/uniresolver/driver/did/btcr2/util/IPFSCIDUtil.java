package uniresolver.driver.did.btcr2.util;

import com.google.protobuf.ByteString;
import io.ipfs.cid.Cid;
import io.ipfs.multihash.Multihash;
import uniresolver.driver.did.btcr2.util.protobuf.Merkledag;
import uniresolver.driver.did.btcr2.util.protobuf.Unixfs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IPFSCIDUtil {

    private static final int MAX_CHUNK_SIZE = 256 * 1024;
    private static final int DEFAULT_CID_VERSION = 1;

    public static Cid createCid(int cidVersion, byte[] dataBytes) throws IOException, NoSuchAlgorithmException {
        if (dataBytes == null) return null;
        if (dataBytes.length > MAX_CHUNK_SIZE) throw new IllegalArgumentException("Data size " + dataBytes.length + " is more than max chunk size " + MAX_CHUNK_SIZE);

        Unixfs.Data.Builder unixfsDataBuilder = Unixfs.Data.newBuilder();
        unixfsDataBuilder.setData(ByteString.copyFrom(dataBytes));
        unixfsDataBuilder.setType(Unixfs.Data.DataType.File);
        unixfsDataBuilder.setFilesize(dataBytes.length);

        Unixfs.Data unixfsData = unixfsDataBuilder.build();
        ByteArrayOutputStream unixfsDataBytes = new ByteArrayOutputStream();
        unixfsData.writeTo(unixfsDataBytes);

        Merkledag.PBNode.Builder merkledagPBNodeBuilder = Merkledag.PBNode.newBuilder();
        merkledagPBNodeBuilder.clear();
        merkledagPBNodeBuilder.setData(ByteString.copyFrom(unixfsDataBytes.toByteArray()));

        Merkledag.PBNode merkledagPBNode = merkledagPBNodeBuilder.build();
        ByteArrayOutputStream merkledagPBNodeBytes = new ByteArrayOutputStream();
        merkledagPBNode.writeTo(merkledagPBNodeBytes);

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] merkledagPBNodeHash = messageDigest.digest(merkledagPBNodeBytes.toByteArray());
        Multihash merkledagPBNodeMultihash = new Multihash(Multihash.Type.sha2_256, merkledagPBNodeHash);

        return Cid.build(cidVersion, Cid.Codec.Raw, merkledagPBNodeMultihash);
    }

    public static Cid createCid(byte[] data) throws IOException, NoSuchAlgorithmException {
        return createCid(DEFAULT_CID_VERSION, data);
    }
}

