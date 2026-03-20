package uniresolver.driver.did.btcr2.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bitcoinj.base.Address;

import java.util.Map;

public class RecordUtil {

    private static final JsonMapper jsonMapper = JsonMapper.builder().build();

    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Address.class, new ToStringSerializer());
        jsonMapper.registerModule(module);
    }

    public static <T extends Record> T fromMap(Map<String, Object> map, Class<T> clazz) {
        if (map == null) return null;
        return jsonMapper.convertValue(map, clazz);
    }

    public static Map<String, Object> toMap(Record record) {
        return jsonMapper.convertValue(record, Map.class);
    }

    public static <T extends Record> T copy(T record, Class<T> clazz) {
        try {
            return jsonMapper.readValue(jsonMapper.writeValueAsString(record), clazz);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}
