package id.unifi.service.common.config;

import com.google.common.net.HostAndPort;
import com.statemachinesystems.envy.ValueParser;

public class HostAndPortValueParser implements ValueParser<HostAndPort> {
    public static final HostAndPortValueParser instance = new HostAndPortValueParser();

    private HostAndPortValueParser() {}

    public HostAndPort parseValue(String value) {
        return HostAndPort.fromString(value);
    }

    public Class<HostAndPort> getValueClass() {
        return HostAndPort.class;
    }
}
