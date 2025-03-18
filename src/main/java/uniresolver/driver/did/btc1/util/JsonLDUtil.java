package uniresolver.driver.did.btc1.util;

import foundation.identity.jsonld.JsonLDObject;

import java.util.List;

public class JsonLDUtil {

    public static boolean containsById(List<? extends JsonLDObject> jsonLdObjectList, JsonLDObject containsJsonLdObject) {
        for (JsonLDObject jsonLdObject : jsonLdObjectList) {
            if (jsonLdObject.getId() != null && jsonLdObject.getId().equals(containsJsonLdObject.getId())) return true;
        }
        return false;
    }
}
