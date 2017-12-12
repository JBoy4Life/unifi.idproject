package id.unifi.service.common.util;

import com.google.common.io.BaseEncoding;

/**
 * Encodes byte array in hex via toString.
 * Useful for deferred evaluation in log statements.
 */
public class HexEncoded {
    private static BaseEncoding hex = BaseEncoding.base16().lowerCase();

    private final byte[] bytes;

    public HexEncoded(byte[] bytes) {
        this.bytes = bytes;
    }

    public String toString() {
        return hex.encode(bytes);
    }
}
