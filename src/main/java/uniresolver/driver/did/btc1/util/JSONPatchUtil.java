package uniresolver.driver.did.btc1.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;
import jakarta.json.JsonMergePatch;
import jakarta.json.JsonValue;

public class JSONPatchUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static DIDDocument apply(DIDDocument didDocument, String patch) {
        try {
            JsonMergePatch jsonMergePatch = objectMapper.readValue(patch, JsonMergePatch.class);
            JsonValue orig = objectMapper.convertValue(didDocument.toMap(), JsonValue.class);
            JsonValue patched = jsonMergePatch.apply(orig);
            return objectMapper.convertValue(patched, DIDDocument.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}
