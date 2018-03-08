package id.unifi.service.common.operator;

import id.unifi.service.common.api.VerticalConfigForApi;
import id.unifi.service.common.security.Token;
import id.unifi.service.common.types.OperatorInfo;

import java.time.Instant;
import java.util.Map;

public class AuthInfo {
    public final OperatorInfo operator;
    public final Token token;
    public final Instant expiry;
    public final Map<String, VerticalConfigForApi> verticalConfig;

    public AuthInfo(OperatorInfo operator,
                    Token token,
                    Instant expiry,
                    Map<String, VerticalConfigForApi> verticalConfig) {
        this.operator = operator;
        this.token = token;
        this.expiry = expiry;
        this.verticalConfig = verticalConfig;
    }
}
