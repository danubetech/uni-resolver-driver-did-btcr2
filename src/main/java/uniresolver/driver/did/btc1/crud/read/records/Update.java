package uniresolver.driver.did.btc1.crud.read.records;

public record Update(Integer targetVersionId, byte[] sourceHash, byte[] targetHash, String patch) {
}
