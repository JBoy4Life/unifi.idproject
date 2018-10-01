package id.unifi.service.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import id.unifi.service.common.types.EnumUtils;

public enum HolderType {
    CONTACT, ASSET;

    private final String stringName;

    HolderType() {
        this.stringName = EnumUtils.getOutputConverter().convert(name());
    }

    @JsonCreator
    public static HolderType fromString(String type) {
        return valueOf(EnumUtils.getInputConverter().convert(type));
    }

    @JsonValue
    public String toString() {
        return stringName;
    }
}
