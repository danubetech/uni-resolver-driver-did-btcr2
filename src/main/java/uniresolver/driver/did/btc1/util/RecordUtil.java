package uniresolver.driver.did.btc1.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bitcoinj.base.Address;

import java.io.IOException;
import java.util.Map;

public class RecordUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Address.class, new ToStringSerializer());
        objectMapper.registerModule(module);
    }

    public static <T extends Record> T fromMap(Map<String, Object> map, Class<T> clazz) {
        if (map == null) return null;
        return objectMapper.convertValue(map, clazz);
    }

    public static Map<String, Object> toMap(Record record) {
        return objectMapper.convertValue(record, Map.class);
    }
}
