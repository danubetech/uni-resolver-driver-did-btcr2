package uniresolver.driver.did.btc1.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;

public class DIDDocumentUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static DIDDocument copy(DIDDocument didDocument) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(didDocument), DIDDocument.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}
