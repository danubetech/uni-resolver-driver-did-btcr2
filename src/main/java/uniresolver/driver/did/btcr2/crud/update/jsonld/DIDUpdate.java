package uniresolver.driver.did.btcr2.crud.update.jsonld;

import com.fasterxml.jackson.annotation.JsonCreator;
import foundation.identity.jsonld.JsonLDObject;
import foundation.identity.jsonld.JsonLDUtils;

import java.io.Reader;
import java.util.List;
import java.util.Map;

public class DIDUpdate extends JsonLDObject {

    @JsonCreator
    public DIDUpdate() {
        super();
    }

    protected DIDUpdate(Map<String, Object> jsonObject) {
        super(jsonObject);
    }

    /*
     * Factory methods
     */

    public static DIDUpdate fromJsonObject(Map<String, Object> jsonObject) {
        return new DIDUpdate(jsonObject);
    }

    public static DIDUpdate fromJsonLDObject(JsonLDObject jsonLDObject) { return fromJsonObject(jsonLDObject.getJsonObject()); }

    public static DIDUpdate fromJson(Reader reader) {
        return new DIDUpdate(readJson(reader));
    }

    public static DIDUpdate fromJson(String json) {
        return new DIDUpdate(readJson(json));
    }

    public static DIDUpdate fromMap(Map<String, Object> map) {
        return new DIDUpdate(map);
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
