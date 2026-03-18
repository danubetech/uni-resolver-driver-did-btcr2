package uniresolver.driver.did.btcr2.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.Map;

public class JSONUtil {

    private static final JsonMapper jsonMapper = JsonMapper.builder().build();

    public static Map<String, Object> jsonToMap(String json) {
        try {
            return jsonMapper.readValue(json, Map.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    public static String mapToJson(Map<String, Object> map) {
        try {
            return jsonMapper.writeValueAsString(map);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}
