package uniresolver.driver.did.btc1.appendix;

import org.bitcoinj.base.Bech32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bech32EncodingAndDecoding {

    private static final Logger log = LoggerFactory.getLogger(Bech32EncodingAndDecoding.class);

    public static String bech32Encode(Bech32.Bech32Data bech32Data) {
        String bech32Encoded = Bech32.encode(bech32Data);
        if (log.isDebugEnabled()) log.debug("bech32Encoded for " + bech32Data + ": " + bech32Encoded);
        return bech32Encoded;
    }

    public static Bech32.Bech32Data bech32Decode(String string) {
        Bech32.Bech32Data bech32Decoded = Bech32.decode(string);
        if (log.isDebugEnabled()) log.debug("bech32Decoded for " + string + ": " + bech32Decoded);
        return bech32Decoded;
    }
}
