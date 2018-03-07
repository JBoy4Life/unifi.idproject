package id.unifi.service.provider.security.gallagher;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class OleDate {

    private ZonedDateTime timestamp;

    OleDate(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double toDouble() {
        // Prepare yourself for the most moronic time representation ever.
        // https://docs.microsoft.com/en-gb/cpp/atl-mfc-shared/reference/coledatetime-class
        final ZonedDateTime oleEpoch = ZonedDateTime.of(1899, 12, 30, 0, 0, 0, 0, ZoneId.of("UTC"));
        final double oleDateTime = Duration.between(oleEpoch, timestamp).toSeconds() / 3600 / 24.0;
        return oleDateTime;
    }

}
