package uniresolver.driver.did.btc1.appendix;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ipfs.api.IPFS;
import io.ipfs.cid.Cid;
import io.ipfs.multihash.Multihash;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FetchContentFromAddressableStorage {

    private static final Logger log = LoggerFactory.getLogger(FetchContentFromAddressableStorage.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static byte[] fetchContentFromAddressableStorage(byte[] hashBytes, IPFS ipfs) {
        Cid cid = Cid.buildCidV1(Cid.Codec.Raw, Multihash.Type.id, hashBytes);
        byte[] content;
        try {
            content = ipfs.get(cid.bareMultihash());
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        if (log.isDebugEnabled()) log.debug("fetchContentFromAddressableStorage for " + Hex.encodeHexString(hashBytes) + " and " + cid + ": " + Hex.encodeHexString(content));
        return content;
    }

    public static <T extends Record> T fetchContentFromAddressableStorage(byte[] hashBytes, Class<T> clazz, IPFS ipfs) {
        byte[] content = fetchContentFromAddressableStorage(hashBytes, ipfs);
        if (content == null) return null;
        T record;
        try {
            record = objectMapper.readValue(content, clazz);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        if (log.isDebugEnabled()) log.debug("fetchContentFromAddressableStorage for " + Hex.encodeHexString(hashBytes) + ": " + record);
        return record;
    }
}
