package uniresolver.driver.did.btc1.crud.update.records;

import io.leonard.Base58;

import java.util.List;
import java.util.Map;

public record DIDUpdatePayload(
        Integer targetVersionId,
        byte[] sourceHash,
        byte[] targetHash,
        List<Map<String, Object>> patch) {

    public static DIDUpdatePayload fromSignalSidecarDataUpdatePayload(Map<String, Object> signalSidecarDataUpdatePayload) {
        Integer targetVersionId = (Integer) signalSidecarDataUpdatePayload.get("targetVersionId");
        byte[] sourceHash = Base58.decode((String) signalSidecarDataUpdatePayload.get("sourceHash"));
        byte[] targetHash = Base58.decode((String) signalSidecarDataUpdatePayload.get("targetHash"));
        List<Map<String, Object>> patch = (List<Map<String, Object>>) signalSidecarDataUpdatePayload.get("patch");
        return new DIDUpdatePayload(targetVersionId, sourceHash, targetHash, patch);
    }
}
