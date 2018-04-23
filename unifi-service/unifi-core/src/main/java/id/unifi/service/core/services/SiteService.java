package id.unifi.service.core.services;

import com.google.common.net.HostAndPort;
import id.unifi.service.common.api.MessageListener;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.types.pk.OperatorPK;
import id.unifi.service.common.rfid.RfidReader;
import id.unifi.service.common.rfid.RfidReaderStatus;
import id.unifi.service.core.DetectionProcessor;
import id.unifi.service.common.operator.OperatorSessionData;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.SITE;
import static id.unifi.service.core.db.Tables.ZONE;
import id.unifi.service.core.site.ResolvedDetection;
import org.eclipse.jetty.websocket.api.Session;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApiService("site")
public class SiteService {
    private final Database db;
    private final DetectionProcessor detectionProcessor;

    public SiteService(DatabaseProvider dbProvider, DetectionProcessor detectionProcessor) {
        this.db = dbProvider.bySchema(CORE);
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
    public List<SiteInfo> listSites(OperatorSessionData session, String clientId) {
        authorize(session, clientId);
        return db.execute(sql -> sql.selectFrom(SITE)
                .where(SITE.CLIENT_ID.eq(clientId))
                .fetch(r -> new SiteInfo(r.getSiteId(), r.getDescription(), r.getAddress())));
    }

    @ApiOperation
    public List<ZoneInfo> listZones(OperatorSessionData session, String clientId, String siteId) {
        authorize(session, clientId);
        return db.execute(sql -> sql.selectFrom(ZONE)
                .where(ZONE.CLIENT_ID.eq(clientId))
                .and(ZONE.SITE_ID.eq(siteId))
                .fetch(r -> new ZoneInfo(r.getZoneId(), r.getName(), r.getDescription())));
    }

    @ApiOperation
    public void subscribeDetections(Session session,
                                    String clientId,
                                    String siteId,
                                    MessageListener<List<ResolvedDetection>> listener) {
        detectionProcessor.addListener(clientId, siteId, session, listener);
    }

    private static OperatorPK authorize(OperatorSessionData sessionData, String clientId) {
        return Optional.ofNullable(sessionData.getOperator())
                .filter(op -> op.clientId.equals(clientId))
                .orElseThrow(Unauthorized::new);
    }



    public static class ZoneInfo {
        public final String zoneId;
        public final String name;
        public final String description;

        ZoneInfo(String zoneId, String name, String description) {
            this.zoneId = zoneId;
            this.name = name;
            this.description = description;
        }
    }

    public class SiteInfo {
        public final String siteId;
        public final String description;
        public final String address;

        SiteInfo(String siteId, String description, String address) {
            this.siteId = siteId;
            this.description = description;
            this.address = address;
        }
    }
}
