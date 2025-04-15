package uniresolver.driver.did.btc1.crud.update.records;

import com.fasterxml.jackson.annotation.JsonCreator;
import foundation.identity.jsonld.JsonLDObject;
import foundation.identity.jsonld.JsonLDUtils;

import java.io.Reader;
import java.util.List;
import java.util.Map;

public class DIDUpdatePayload extends JsonLDObject {

    @JsonCreator
    public DIDUpdatePayload() {
        super();
    }

    protected DIDUpdatePayload(Map<String, Object> jsonObject) {
        super(jsonObject);
    }

    /*
     * Factory methods
     */

    public static DIDUpdatePayload fromJsonObject(Map<String, Object> jsonObject) {
        return new DIDUpdatePayload(jsonObject);
    }

    public static DIDUpdatePayload fromJsonLDObject(JsonLDObject jsonLDObject) { return fromJsonObject(jsonLDObject.getJsonObject()); }

    public static DIDUpdatePayload fromJson(Reader reader) {
        return new DIDUpdatePayload(readJson(reader));
    }

    public static DIDUpdatePayload fromJson(String json) {
        return new DIDUpdatePayload(readJson(json));
    }

    public static DIDUpdatePayload fromMap(Map<String, Object> map) {
        return new DIDUpdatePayload(map);
    }

    /*
     * Getters
     */

    public Integer getTargetVersionId() {
        Object jsonValue = JsonLDUtils.jsonLdGetJsonValue(this.getJsonObject(), "targetVersionId");
        return (Integer) jsonValue;
    }

    public String getSourceHash() {
        String jsonString = JsonLDUtils.jsonLdGetString(this.getJsonObject(), "sourceHash");
        return jsonString;
    }

    public String getTargetHash() {
        String jsonString = JsonLDUtils.jsonLdGetString(this.getJsonObject(), "targetHash");
        return jsonString;
    }

    public List<Map<String, Object>> getPatch() {
        Object jsonValue = JsonLDUtils.jsonLdGetJsonValue(this.getJsonObject(), "patch");
        return (List<Map<String, Object>>) jsonValue;
    }
}
