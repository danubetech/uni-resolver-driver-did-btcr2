package uniresolver.driver.did.btcr2.util;

import java.util.Base64;

public class Base64Util {

    private static final Base64.Encoder BASE64_URL_WITHOUT_PADDING_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    public static String base64UrlWithoutPaddingEncodeToString(byte[] bytes) {
        return bytes == null ? null : BASE64_URL_WITHOUT_PADDING_ENCODER.encodeToString(bytes);
    }

    public static byte[] base64UrlDecode(String string) {
        return string == null ? null : BASE64_URL_DECODER.decode(string);
    }
}
