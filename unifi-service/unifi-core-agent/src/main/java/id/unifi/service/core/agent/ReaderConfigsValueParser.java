package id.unifi.service.core.agent;

import com.statemachinesystems.envy.ValueParser;

public class ReaderConfigsValueParser implements ValueParser<ReaderConfigs> {
    public static final ReaderConfigsValueParser instance = new ReaderConfigsValueParser();

    private ReaderConfigsValueParser() {}

    public ReaderConfigs parseValue(String value) {
        return ReaderConfigs.fromString(value);
    }

    public Class<ReaderConfigs> getValueClass() {
        return ReaderConfigs.class;
    }
}
