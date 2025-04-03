package uniresolver.driver.did.btc1.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonPatch;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class JSONPatchUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static DIDDocument apply(DIDDocument didDocument, List<Map<String, Object>> patch) {
        JsonPatch jsonPatch = Json.createPatch(Json.createArrayBuilder(patch).build());
        JsonObject didDocumentObject = Json.createObjectBuilder(didDocument.toMap()).build();
        JsonObject patchedDidDocumentObject = jsonPatch.apply(didDocumentObject);
        StringWriter stringWriter = new StringWriter();
        Json.createWriter(stringWriter).write(patchedDidDocumentObject);
        return DIDDocument.fromJson(stringWriter.toString());
    }
}
