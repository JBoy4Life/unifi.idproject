package id.unifi.service.provider.security.gallagher;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OleDateTimeTest {

    @Test
    void unixEpoch() {
        var value = oleDateTimeFromUtc(LocalDateTime.of(1970, 1, 1, 0, 0, 0, 0));
        assertEquals(25569, value);
    }

    @Test
    void oleEpoch() {
        var value = oleDateTimeFromUtc(LocalDateTime.of(1899, 12, 30, 0, 0, 0, 0));
        assertEquals(0, value);
    }

    @Test
    void arbitraryDate() {
        var value = oleDateTimeFromUtc(LocalDateTime.of(1970, 1, 2, 6, 0, 0, 0));
        assertEquals(25570.25, value);
    }

    @Test
    void preciseTest() {
        var value = oleDateTimeFromUtc(LocalDateTime.of(2018, 4, 21, 15, 35, 14, 0));
        assertEquals(43211.649467593, value, 1e-8);
    }

    private double oleDateTimeFromUtc(LocalDateTime utcDateTime) {
        return OleDateTime.fromInstant(utcDateTime.toInstant(ZoneOffset.UTC));
    }
}
