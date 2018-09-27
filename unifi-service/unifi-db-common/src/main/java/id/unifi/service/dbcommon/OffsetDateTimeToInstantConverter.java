package id.unifi.service.dbcommon;

import static java.time.ZoneOffset.UTC;
import org.jooq.impl.AbstractConverter;

import java.time.Instant;
import java.time.OffsetDateTime;

public class OffsetDateTimeToInstantConverter extends AbstractConverter<OffsetDateTime, Instant> {
    public OffsetDateTimeToInstantConverter() {
        super(OffsetDateTime.class, Instant.class);
    }

    public Instant from(OffsetDateTime date) {
        return date == null ? null : date.toInstant();
    }

    public OffsetDateTime to(Instant instant) {
        return instant == null ? null : instant.atOffset(UTC);
    }
}
