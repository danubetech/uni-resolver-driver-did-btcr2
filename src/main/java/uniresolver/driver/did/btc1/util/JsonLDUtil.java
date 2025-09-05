package uniresolver.driver.did.btc1.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.jsonld.JsonLDObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class JsonLDUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T extends JsonLDObject> T copy(T jsonLDObject, Class<T> clazz) {
        try {
            Method fromMapMethod = clazz.getMethod("fromMap", Map.class);
            return (T) fromMapMethod.invoke(null, jsonLDObject.toMap());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}
