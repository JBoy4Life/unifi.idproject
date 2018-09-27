package id.unifi.service.dbcommon;

import static java.time.ZoneOffset.UTC;
import org.jooq.Converter;

import java.time.Instant;
import java.time.OffsetDateTime;

public class OffsetDateTimeInstantConverter implements Converter<OffsetDateTime, Instant> {
    public Instant from(OffsetDateTime date) {
        return date.toInstant();
    }

    public OffsetDateTime to(Instant instant) {
        return instant.atOffset(UTC);
    }

    public Class<OffsetDateTime> fromType() {
        return OffsetDateTime.class;
    }

    public Class<Instant> toType() {
        return Instant.class;
    }
}
