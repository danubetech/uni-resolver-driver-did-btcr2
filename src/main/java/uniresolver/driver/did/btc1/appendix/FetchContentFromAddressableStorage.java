package uniresolver.driver.did.btc1.appendix;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.jsonld.JsonLDObject;
import io.ipfs.cid.Cid;
import io.ipfs.multihash.Multihash;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.did.btc1.connections.ipfs.IPFSConnection;

import java.io.IOException;

public class FetchContentFromAddressableStorage {

    private static final Logger log = LoggerFactory.getLogger(FetchContentFromAddressableStorage.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static byte[] fetchContentFromAddressableStorage(byte[] hashBytes, IPFSConnection ipfsConnection) {
        Cid cid = Cid.buildV0(new Multihash(Multihash.Type.sha2_256, hashBytes));
        byte[] content;
        try {
            content = ipfsConnection.getIpfs().get(cid.bareMultihash());
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        if (log.isDebugEnabled()) log.debug("fetchContentFromAddressableStorage for " + Hex.encodeHexString(hashBytes) + " and " + cid + ": " + Hex.encodeHexString(content));
        return content;
    }

    public static <T extends Record> T fetchRecordContentFromAddressableStorage(byte[] hashBytes, Class<T> clazz, IPFSConnection ipfsConnection) {
        byte[] content = fetchContentFromAddressableStorage(hashBytes, ipfsConnection);
        if (content == null) return null;
        T record;
        try {
            record = objectMapper.readValue(content, clazz);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        if (log.isDebugEnabled()) log.debug("fetchRecordContentFromAddressableStorage for " + Hex.encodeHexString(hashBytes) + ": " + record);
        return record;
    }

    public static <T extends JsonLDObject> T fetchJsonLDObjectContentFromAddressableStorage(byte[] hashBytes, Class<T> clazz, IPFSConnection ipfsConnection) {
        byte[] content = fetchContentFromAddressableStorage(hashBytes, ipfsConnection);
        if (content == null) return null;
        T record;
        try {
            record = objectMapper.readValue(content, clazz);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        if (log.isDebugEnabled()) log.debug("fetchJsonLDObjectContentFromAddressableStorage for " + Hex.encodeHexString(hashBytes) + ": " + record);
        return record;
    }
}
