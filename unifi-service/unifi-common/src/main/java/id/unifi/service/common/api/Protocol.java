package id.unifi.service.common.api;

import com.google.common.base.CaseFormat;

public enum Protocol {
    JSON, MSGPACK;

    public String toString() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, name());
    }
}
