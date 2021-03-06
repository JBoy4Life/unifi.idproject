package id.unifi.service.core.services;

import com.google.common.net.HostAndPort;
import id.unifi.service.common.api.MessageListener;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.NotFound;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.rfid.RfidReader;
import id.unifi.service.common.rfid.RfidReaderStatus;
import id.unifi.service.common.types.pk.OperatorPK;
import id.unifi.service.common.types.pk.SitePK;
import id.unifi.service.common.types.pk.ZonePK;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.SITE;
import static id.unifi.service.core.db.Tables.ZONE;
import id.unifi.service.core.processing.listener.DetectionSubscriber;
import id.unifi.service.core.site.ResolvedSiteDetection;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApiService("site")
public class SiteService {
    private final Database db;
    private final DetectionSubscriber detectionSubscriber;

    public SiteService(DatabaseProvider dbProvider, DetectionSubscriber detectionSubscriber) {
        this.db = dbProvider.bySchema(CORE);
        this.detectionSubscriber = detectionSubscriber;
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
                .fetch(r -> new SiteInfo(r.getSiteId(), r.getDescription(), r.getAddress(), r.getTimeZone())));
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
    public void subscribeDetections(OperatorSessionData session,
                                    String clientId,
                                    String siteId,
                                    @Nullable Boolean includeLastKnown,
                                    MessageListener<List<ResolvedSiteDetection>> listener) {
        authorize(session, clientId);
        var site = new SitePK(clientId, siteId);
        
        var siteExists = db.execute(sql ->
                sql.fetchExists(SITE, SITE.CLIENT_ID.eq(site.clientId).and(SITE.SITE_ID.eq(site.siteId))));
        if (!siteExists) {
            throw new NotFound("site");
        }

        detectionSubscriber.addListener(site, listener, Boolean.TRUE.equals(includeLastKnown));
    }

    @ApiOperation
    public void subscribeZoneDetections(OperatorSessionData session,
                                        String clientId,
                                        String siteId,
                                        String zoneId,
                                        @Nullable Boolean includeLastKnown,
                                        MessageListener<List<ResolvedSiteDetection>> listener) {
        authorize(session, clientId);
        var zone = new ZonePK(clientId, siteId, zoneId);

        var zoneExists = db.execute(sql ->
                sql.fetchExists(ZONE, ZONE.CLIENT_ID.eq(zone.clientId)
                        .and(ZONE.SITE_ID.eq(zone.siteId))
                        .and(ZONE.ZONE_ID.eq(zone.zoneId))));
        if (!zoneExists) {
            throw new NotFound("zone");
        }

        detectionSubscriber.addListener(zone, listener, Boolean.TRUE.equals(includeLastKnown));
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

    public static class SiteInfo {
        public final String siteId;
        public final String description;
        public final String address;
        public final String timeZone;

        SiteInfo(String siteId, String description, String address, String timeZone) {
            this.siteId = siteId;
            this.description = description;
            this.address = address;
            this.timeZone = timeZone;
        }
    }
}
