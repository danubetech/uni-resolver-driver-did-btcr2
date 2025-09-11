package uniresolver.driver.did.btcr2.util;

import java.net.MalformedURLException;
import java.net.URL;

public class URLUtil {

    public static URL url(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
