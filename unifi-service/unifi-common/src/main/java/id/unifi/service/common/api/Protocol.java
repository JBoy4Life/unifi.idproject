package id.unifi.service.common.api;

import com.google.common.base.CaseFormat;

public enum Protocol {
    JSON(false), MSGPACK(true);

    private final boolean binary;
    private final String stringName;

    Protocol(boolean binary) {
        this.binary = binary;
        this.stringName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, name());
    }

    public boolean isBinary() {
        return binary;
    }

    public String toString() {
        return stringName;
    }
}
