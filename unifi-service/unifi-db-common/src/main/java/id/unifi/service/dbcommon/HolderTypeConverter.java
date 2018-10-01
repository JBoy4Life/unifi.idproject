package id.unifi.service.dbcommon;

import id.unifi.service.common.HolderType;
import org.jooq.impl.EnumConverter;

public class HolderTypeConverter extends EnumConverter<String, HolderType> {
    public HolderTypeConverter() {
        super(String.class, HolderType.class);
    }

    @Override
    public String to(HolderType userObject) {
        return userObject.toString();
    }
}
