package uniresolver.driver.did.btcr2.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class HexUtil {

    public static String hexEncode(byte[] bytes) {
        return Hex.encodeHexString(bytes);
    }

    public static byte[] hexDecode(String string) {
        try {
            return Hex.decodeHex(string);
        } catch (DecoderException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
