package id.unifi.service.common.operator;

import id.unifi.service.common.security.Token;

import java.time.Instant;

public class ExpiringToken {
    public final Token token;
    public final Instant expiry;

    public ExpiringToken(Token token, Instant expiry) {
        this.token = token;
        this.expiry = expiry;
    }
}
