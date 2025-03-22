package uniresolver.driver.did.btc1.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.jsonld.JsonLDObject;
import org.erdtman.jcs.JsonCanonicalizer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JsonCanonicalizationAndHashUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static byte[] jsonCanonicalizationAndHash(String json) {
        String encodedString;
        try {
            encodedString = new JsonCanonicalizer(json).getEncodedString();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
        return SHA256Util.sha256(encodedString.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] jsonCanonicalizationAndHash(Record record) {
        try {
            return jsonCanonicalizationAndHash(objectMapper.writeValueAsString(record));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    public static byte[] jsonCanonicalizationAndHash(Map<String, Object> map) {
        try {
            return jsonCanonicalizationAndHash(objectMapper.writeValueAsString(map));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    public static byte[] jsonCanonicalizationAndHash(JsonLDObject jsonLDObject) {
        return jsonCanonicalizationAndHash(jsonLDObject.toMap());
    }
}
