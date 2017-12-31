package id.unifi.service.common.util;

import static java.time.ZoneOffset.UTC;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDateTime;

public class TimeUtils {
    public static Instant instantFromUtcLocal(@Nullable  LocalDateTime date) {
        return date == null ? null : date.toInstant(UTC);
    }

    public static LocalDateTime utcLocalFromInstant(Instant instant) {
        return LocalDateTime.ofInstant(instant, UTC);
    }
}
