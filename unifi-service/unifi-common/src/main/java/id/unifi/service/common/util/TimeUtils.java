package id.unifi.service.common.util;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDateTime;

public class TimeUtils {
    public static Instant instantFromUtcLocal(LocalDateTime date) {
        return date.toInstant(UTC);
    }

    public static LocalDateTime utcLocalFromInstant(Instant instant) {
        return LocalDateTime.ofInstant(instant, UTC);
    }
}
