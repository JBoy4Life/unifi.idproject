package id.unifi.service.core.util;

import com.amazonaws.regions.Regions;
import com.statemachinesystems.envy.ValueParser;

public class RegionsValueParser implements ValueParser<Regions> {
    public static final ValueParser<Regions> instance = new RegionsValueParser();

    private RegionsValueParser() {}

    public Regions parseValue(String value) {
        return Regions.fromName(value);
    }

    public Class<Regions> getValueClass() {
        return Regions.class;
    }
}
