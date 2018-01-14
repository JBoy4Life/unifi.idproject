package id.unifi.service.common.util;

import static java.time.ZoneOffset.UTC;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtils {
    private static final ZoneId timeZoneId = ZoneId.of("Europe/London");

    public static Instant instantFromUtcLocal(@Nullable LocalDateTime date) {
        return date == null ? null : date.toInstant(UTC);
    }

    public static ZonedDateTime zonedFromUtcLocal(@Nullable LocalDateTime date) {
        return date == null ? null : instantFromUtcLocal(date).atZone(timeZoneId);
    }

    public static LocalDateTime utcLocalFromInstant(Instant instant) {
        return LocalDateTime.ofInstant(instant, UTC);
    }
}
