package id.unifi.service.core.services;

import com.google.common.net.HostAndPort;
import id.unifi.service.common.api.MessageListener;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import static id.unifi.service.common.db.DatabaseProvider.CORE_SCHEMA_NAME;
import id.unifi.service.common.rfid.RfidReader;
import id.unifi.service.common.rfid.RfidReaderStatus;
import id.unifi.service.core.DetectionProcessor;
import id.unifi.service.core.OperatorSessionData;
import static id.unifi.service.core.db.Tables.HOLDER;
import static id.unifi.service.core.db.Tables.ZONE;
import id.unifi.service.core.site.ResolvedDetection;
import org.eclipse.jetty.websocket.api.Session;

import java.util.List;
import java.util.Map;

@ApiService("site")
public class SiteService {
    private final Database db;
    private final DetectionProcessor detectionProcessor;

    public SiteService(DatabaseProvider dbProvider, DetectionProcessor detectionProcessor) {
        this.db = dbProvider.bySchemaName(CORE_SCHEMA_NAME);
        this.detectionProcessor = detectionProcessor;
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

    @ApiOperation
    public List<ZoneInfo> listZones(String clientId, String siteId) {
        return db.execute(sql -> sql.selectFrom(ZONE)
                .where(ZONE.CLIENT_ID.eq(clientId))
                .and(ZONE.SITE_ID.eq(siteId))
                .fetch(r -> new ZoneInfo(r.getZoneId(), r.getName(), r.getDescription())));
    }

    @ApiOperation
    public List<HolderInfo> listHolders(String clientId) {
        return db.execute(sql -> sql.selectFrom(HOLDER)
                .where(HOLDER.CLIENT_ID.eq(clientId))
                .fetch(r -> new HolderInfo(r.getClientReference(), r.getName(), r.getHolderType(), r.getActive())));
    }

    @ApiOperation
    public void subscribeDetections(Session session,
                                    String clientId,
                                    String siteId,
                                    MessageListener<List<ResolvedDetection>> listener) {
        detectionProcessor.addListener(clientId, siteId, session, listener);
    }

    public class ZoneInfo {
        public final String zoneId;
        public final String name;
        public final String description;

        public ZoneInfo(String zoneId, String name, String description) {
            this.zoneId = zoneId;
            this.name = name;
            this.description = description;
        }
    }

    private class HolderInfo {
        public final String clientReference;
        public final String name;
        public final String holderType;
        public final Boolean active;

        public HolderInfo(String clientReference, String name, String holderType, Boolean active) {
            this.clientReference = clientReference;
            this.name = name;
            this.holderType = holderType;
            this.active = active;
        }
    }
}
