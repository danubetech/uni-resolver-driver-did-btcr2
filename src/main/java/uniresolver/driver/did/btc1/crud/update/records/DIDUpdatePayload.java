package uniresolver.driver.did.btc1.crud.update.records;

public record DIDUpdatePayload(
        Integer targetVersionId,
        byte[] sourceHash,
        byte[] targetHash,
        String patch) {
}
