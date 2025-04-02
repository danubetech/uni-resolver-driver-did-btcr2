package uniresolver.driver.did.btc1.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class RecordUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T extends Record> T fromMap(Map<String, Object> map, Class<T> clazz) {
        if (map == null) return null;
        return objectMapper.convertValue(map, clazz);
    }

    public static Map<String, Object> toMap(Record record) {
        return objectMapper.convertValue(record, Map.class);
    }
}
