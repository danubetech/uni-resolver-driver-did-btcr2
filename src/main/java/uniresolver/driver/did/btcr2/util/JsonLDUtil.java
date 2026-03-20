package uniresolver.driver.did.btcr2.util;

import foundation.identity.jsonld.JsonLDObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class JsonLDUtil {

    public static <T extends JsonLDObject> T copy(T jsonLDObject, Class<T> clazz) {
        try {
            Method fromMapMethod = clazz.getMethod("fromMap", Map.class);
            return (T) fromMapMethod.invoke(null, jsonLDObject.toMap());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}
