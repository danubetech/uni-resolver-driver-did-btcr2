package uniresolver.driver.did.btc1.crud.update.records;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;

import java.util.List;
import java.util.Map;

public record DIDUpdatePayload(
        Integer targetVersionId,
        byte[] sourceHash,
        byte[] targetHash,
        String patch) {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static DIDUpdatePayload fromSignalSidecarDataUpdatePayload(Map<String, Object> signalSidecarDataUpdatePayload) {
        Integer targetVersionId = (Integer) signalSidecarDataUpdatePayload.get("targetVersionId");
        byte[] sourceHash = Base64.decodeBase64((String) signalSidecarDataUpdatePayload.get("sourceHash"));
        byte[] targetHash = Base64.decodeBase64((String) signalSidecarDataUpdatePayload.get("targetHash"));
        String patch;
        try {
            List<Map<String, Object>> patchList = (List<Map<String, Object>>) signalSidecarDataUpdatePayload.get("patch");
            patch = objectMapper.writeValueAsString(patchList);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        return new DIDUpdatePayload(targetVersionId, sourceHash, targetHash, patch);
    }
}
