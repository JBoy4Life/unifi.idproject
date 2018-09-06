package id.unifi.service.core.processing;

import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.types.pk.DetectablePK;
import id.unifi.service.core.CoreService;
import id.unifi.service.core.db.Keys;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import id.unifi.service.dbcommon.DatabaseUtils;
import org.jooq.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static id.unifi.service.common.util.TimeUtils.utcLocalFromInstant;
import static id.unifi.service.common.util.TimeUtils.utcLocalFromZoned;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.*;
import static org.jooq.impl.DSL.*;

public class VisitProcessor {
    private static final Logger log = LoggerFactory.getLogger(CoreService.class);
    private static final String VISIT_CUTOFF_TIME = "05:00";
    private final Database db;
    private static Field[] detectablePkArray = {RFID_DETECTION.CLIENT_ID, RFID_DETECTION.DETECTABLE_ID,
            RFID_DETECTION.DETECTABLE_TYPE};

    public VisitProcessor(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE, CORE);
    }

    public void insertVisits(String timeZone) {
        log.info("Visit calculation starting at {}", timeZone);
        var now = ZonedDateTime.now(ZoneId.of(timeZone));
        var endTime = now.with(LocalTime.parse(VISIT_CUTOFF_TIME));
        var startTime = endTime.minusDays(1);
        var detectedYesterday = getDetectedDetectables(startTime.toInstant(), endTime.toInstant());

        db.execute(sql -> {
            for (var detectable : detectedYesterday) {
                sql.insertInto(CORE.VISIT).
                        select(
                                (select(RFID_DETECTION.CLIENT_ID, ASSIGNMENT.CLIENT_REFERENCE,
                                        RFID_DETECTION.DETECTION_TIME.minOver()
                                                .partitionBy(RFID_DETECTION.CLIENT_ID,
                                                        RFID_DETECTION.DETECTABLE_ID, RFID_DETECTION.READER_SN),
                                        RFID_DETECTION.DETECTION_TIME.maxOver()
                                                .partitionBy(RFID_DETECTION.CLIENT_ID,
                                                        RFID_DETECTION.DETECTABLE_ID, RFID_DETECTION.READER_SN),
                                        val("measured-day"),
                                        READER.SITE_ID
                                ).distinctOn(detectablePkArray)
                                        .from(CORE.RFID_DETECTION)
                                        .innerJoin(CORE.READER).on(RFID_DETECTION.CLIENT_ID.eq(READER.CLIENT_ID)
                                                .and(RFID_DETECTION.READER_SN.eq(READER.READER_SN)))
                                        .innerJoin(CORE.ASSIGNMENT).on(RFID_DETECTION.DETECTABLE_ID.eq(ASSIGNMENT.DETECTABLE_ID)
                                                .and(RFID_DETECTION.DETECTABLE_TYPE.eq(ASSIGNMENT.DETECTABLE_TYPE)
                                                        .and(RFID_DETECTION.CLIENT_ID.eq(ASSIGNMENT.CLIENT_ID))))
                                        .innerJoin(CORE.SITE).on(RFID_DETECTION.CLIENT_ID.eq(SITE.CLIENT_ID)
                                                .and(READER.SITE_ID.eq(SITE.SITE_ID)))
                                        .where(RFID_DETECTION.DETECTION_TIME.between(utcLocalFromZoned(startTime),
                                                utcLocalFromZoned(endTime)))
                                        .and(SITE.TIME_ZONE.eq(timeZone))
                                        .and(RFID_DETECTION.CLIENT_ID.eq(detectable.clientId))
                                        .and(RFID_DETECTION.DETECTABLE_ID.eq(detectable.detectableId))
                                        .and(RFID_DETECTION.DETECTABLE_TYPE.eq(detectable.detectableType.toString()))
                                        .andExists(selectOne().from(CORE.CONTACT)
                                                .where(CORE.CONTACT.CLIENT_REFERENCE.eq(ASSIGNMENT.CLIENT_REFERENCE)
                                                )))
                        ).onConflictDoNothing().execute();
            }
            log.info("Visit tuples inserted for {}: {}", timeZone, detectedYesterday.size());
            return null;
        });
    }

    private List<DetectablePK> getDetectedDetectables(Instant from, Instant to) {
        return db.execute(sql -> sql.selectDistinct(detectablePkArray)
                .from(CORE.RFID_DETECTION)
                .where(RFID_DETECTION.DETECTION_TIME.between(utcLocalFromInstant(from), utcLocalFromInstant(to)))
                .groupBy(detectablePkArray)
                .having(RFID_DETECTION.DETECTION_TIME.count().gt(1))
                .fetch(r -> new DetectablePK(r.get(RFID_DETECTION.CLIENT_ID),
                        r.get(RFID_DETECTION.DETECTABLE_ID),
                        DetectableType.fromString(r.get(RFID_DETECTION.DETECTABLE_TYPE))))); //TODO: FILTER BY ZoneId
    }
}
