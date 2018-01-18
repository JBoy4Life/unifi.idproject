package id.unifi.service.common.operator;

import id.unifi.service.common.security.Token;

import java.time.Instant;

public class ExpiringToken {
    public final OperatorPK operator;
    public final Token token;
    public final Instant expiry;

    public ExpiringToken(OperatorPK operator, Token token, Instant expiry) {
        this.operator = operator;
        this.token = token;
        this.expiry = expiry;
    }
}
