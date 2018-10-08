package id.unifi.service.common.util;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.temporal.ChronoUnit.SECONDS;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.TemporalQuery;

public class TimeUtils {

    public static final TemporalQuery<BigDecimal> UNIX_TIMESTAMP = temporal -> {
        var instant = Instant.from(temporal);
        var seconds = BigDecimal.valueOf(instant.getEpochSecond());
        var nanos = BigDecimal.valueOf(instant.getNano(), 9);
        return seconds.add(nanos);
    };

    public static String filenameFormattedLocalDateTimeNow() {
        return LocalDateTime.now().truncatedTo(SECONDS).format(ISO_LOCAL_DATE_TIME).replaceAll(":", "-");
    }
}
