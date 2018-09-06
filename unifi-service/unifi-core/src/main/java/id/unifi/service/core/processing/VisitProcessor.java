package id.unifi.service.core.processing;

import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.types.pk.DetectablePK;
import id.unifi.service.core.CoreService;
import id.unifi.service.core.db.Keys;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
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
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectOne;
import static org.jooq.impl.DSL.val;

public class VisitProcessor {
    private static final Logger log = LoggerFactory.getLogger(CoreService.class);
    private final Database db;

    public VisitProcessor(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
    }

    public void insertVisits(String timeZone) {
        log.info("Visit calculation starting at {}", timeZone);
        var now = ZonedDateTime.now(ZoneId.of(timeZone));
        var endTime = now.with(LocalTime.parse("05:00"));
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
                                ).distinctOn(Keys.DETECTABLE_PKEY.getFieldsArray())
                                        .from(CORE.RFID_DETECTION)
                                        .join(CORE.READER).onKey()
                                        .join(CORE.ASSIGNMENT).onKey()
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
        return db.execute(sql -> sql.selectDistinct()
                .from(CORE.RFID_DETECTION)
                .where(RFID_DETECTION.DETECTION_TIME.between(utcLocalFromInstant(from), utcLocalFromInstant(to)))
                .groupBy(Keys.DETECTABLE_PKEY.getFieldsArray())
                .having(RFID_DETECTION.DETECTION_TIME.count().gt(1))
                .fetch(r -> new DetectablePK(r.get(RFID_DETECTION.CLIENT_ID),
                        r.get(RFID_DETECTION.DETECTABLE_ID),
                        DetectableType.fromString(r.get(RFID_DETECTION.DETECTABLE_TYPE))))); //TODO: FILTER BY ZoneId
    }
}
