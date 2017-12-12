package id.unifi.service.common.operator;

import java.time.Instant;

public class ExpiringToken {
    public final byte[] token;
    public final Instant expiry;

    public ExpiringToken(byte[] token, Instant expiry) {
        this.token = token;
        this.expiry = expiry;
    }
}
