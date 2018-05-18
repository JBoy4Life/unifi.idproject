package id.unifi.service.common.config;

import com.google.common.io.BaseEncoding;
import com.statemachinesystems.envy.ValueParser;

public class HexByteArrayValueParser implements ValueParser<byte[]> {
    public static final HexByteArrayValueParser instance = new HexByteArrayValueParser();
    private static BaseEncoding hex = BaseEncoding.base16().lowerCase();

    private HexByteArrayValueParser() {}

    public byte[] parseValue(String value) {
        return hex.decode(value);
    }

    public Class<byte[]> getValueClass() {
        return byte[].class;
    }
}
