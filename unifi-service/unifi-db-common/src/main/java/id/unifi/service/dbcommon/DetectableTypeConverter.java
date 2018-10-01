package id.unifi.service.dbcommon;

import id.unifi.service.common.detection.DetectableType;
import org.jooq.impl.EnumConverter;

public class DetectableTypeConverter extends EnumConverter<String, DetectableType> {
    public DetectableTypeConverter() {
        super(String.class, DetectableType.class);
    }

    @Override
    public String to(DetectableType userObject) {
        return userObject.toString();
    }
}
