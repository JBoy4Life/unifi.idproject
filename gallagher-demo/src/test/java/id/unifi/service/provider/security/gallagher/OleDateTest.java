package id.unifi.service.provider.security.gallagher;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OleDateTest {

    @Test
    void unixEpoch() {
        OleDate oleDate = new OleDate(ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")));
        double value = oleDate.toDouble();
        assertEquals(25569, value);
    }

    @Test
    void oleEpoch() {
        OleDate oleDate = new OleDate(ZonedDateTime.of(1899, 12, 30, 0, 0, 0, 0, ZoneId.of("UTC")));
        double value = oleDate.toDouble();
        assertEquals(0, value);
    }

    @Test
    void arbitraryDate() {
        OleDate oleDate = new OleDate(ZonedDateTime.of(1970, 1, 2, 6, 0, 0, 0, ZoneId.of("UTC")));
        double value = oleDate.toDouble();
        assertEquals(25570.25, value);
    }

}
