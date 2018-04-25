package id.unifi.service.provider.security.gallagher;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

final class OleDateTime {
    private OleDateTime() {}

    private static final Instant OLE_EPOCH = LocalDateTime.of(1899, 12, 30, 0, 0, 0, 0).toInstant(ZoneOffset.UTC);

    static double fromInstant(Instant timestamp) {
        // Prepare yourself for the most moronic time representation ever.
        // https://docs.microsoft.com/en-gb/cpp/atl-mfc-shared/reference/coledatetime-class
        return Duration.between(OLE_EPOCH, timestamp).toSeconds() / 86400.0;
    }
}
