package id.unifi.service.common.detection;

import com.google.common.base.CaseFormat;

public enum DetectableType {
    UHF_EPC, UHF_TID;

    private final String stringName;

    DetectableType() {
        this.stringName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, name());
    }

    public String toString() {
        return stringName;
    }
}
