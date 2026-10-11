package uniresolver.driver.did.btcr2.data;

import com.fasterxml.jackson.databind.json.JsonMapper;
import uniresolver.driver.did.btcr2.util.Base64Util;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SMTProof extends LinkedHashMap<String, Object> {

    private static final JsonMapper jsonMapper = JsonMapper.builder()
            .build();

    public SMTProof() {
        super();
    }

    public SMTProof(Map<? extends String, ? extends String> m) {
        super(m);
    }

    public SMTProof(byte[] id, byte[] nonce, byte[] updateId, byte[] collapsed, List<byte[]> hashes) {
        if (id != null) this.put("id", Base64Util.base64UrlWithoutPaddingEncodeToString(id));
        if (nonce != null) this.put("nonce", Base64Util.base64UrlWithoutPaddingEncodeToString(nonce));
        if (updateId != null) this.put("updateId", Base64Util.base64UrlWithoutPaddingEncodeToString(updateId));
        if (collapsed != null) this.put("collapsed", Base64Util.base64UrlWithoutPaddingEncodeToString(collapsed));
        if (hashes != null) this.put("hashes", hashes.stream().map(Base64Util::base64UrlWithoutPaddingEncodeToString).toList());
    }

    public static SMTProof fromJson(Reader reader) throws IOException {
        return jsonMapper.readValue(reader, SMTProof.class);
    }

    public Map<String, Object> toMap() {
        return this;
    }

    public byte[] getId() {
        return this.containsKey("id") ? Base64Util.base64UrlDecode((String) this.get("id")) : null;
    }

    public byte[] getNonce() {
        return this.containsKey("nonce") ? Base64Util.base64UrlDecode((String) this.get("nonce")) : null;
    }

    public byte[] getUpdateId() {
        return this.containsKey("updateId") ? Base64Util.base64UrlDecode((String) this.get("updateId")) : null;
    }

    public byte[] getCollapsed() {
        return this.containsKey("collapsed") ? Base64Util.base64UrlDecode((String) this.get("collapsed")) : null;
    }

    public List<byte[]> getHashes() {
        return this.containsKey("hashes") ? ((List<String>) this.get("hashes")).stream().map(Base64Util::base64UrlDecode).toList() : null;
    }
}