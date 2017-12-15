package id.unifi.service.core.services;

import com.google.common.net.HostAndPort;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import static id.unifi.service.common.db.DatabaseProvider.CORE_SCHEMA_NAME;
import id.unifi.service.common.rfid.RfidReader;
import id.unifi.service.common.rfid.RfidReaderStatus;
import id.unifi.service.core.OperatorSessionData;

import java.util.List;
import java.util.Map;

@ApiService("site")
public class SiteService {
    private final Database db;

    public SiteService(DatabaseProvider dbProvider) {
        db = dbProvider.bySchemaName(CORE_SCHEMA_NAME);
    }

    @ApiOperation
    public List<RfidReader> discoverReaders(OperatorSessionData session) {
        if (session.getOperator() == null) {
            throw new RuntimeException("Not logged in");
        }
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return List.of(
                new RfidReader("370-17-09-0614", "Speedway R420", new RfidReaderStatus(
                        HostAndPort.fromString("192.168.42.167:5084"),
                        "5.12.2.240",
                        Map.of(1, true, 2, false, 3, true, 4, false))),
                new RfidReader("370-17-09-0615", "Speedway R420", new RfidReaderStatus(
                        HostAndPort.fromString("192.168.42.168:5084"),
                        "5.12.2.240",
                        Map.of(1, true, 2, true, 3, true, 4, true))));
    }
}
