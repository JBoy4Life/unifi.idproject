package id.unifi.service.common.detection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import id.unifi.service.common.types.EnumUtils;

public enum DetectableType {
    UHF_EPC, UHF_TID, MIFARE_CSN, PROX_ID;

    private final String stringName;

    DetectableType() {
        this.stringName = EnumUtils.getOutputConverter().convert(name());
    }

    @JsonCreator
    public static DetectableType fromString(String type) {
        return valueOf(EnumUtils.getInputConverter().convert(type));
    }

    @JsonValue
    public String toString() {
        return stringName;
    }
}
