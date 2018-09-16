package id.unifi.service.core.processing;

import id.unifi.service.core.CoreService;
import id.unifi.service.core.db.Keys;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static id.unifi.service.common.util.TimeUtils.utcLocalFromZoned;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.*;
import static org.jooq.impl.DSL.*;

public class VisitProcessor {
    private static final Logger log = LoggerFactory.getLogger(CoreService.class);
    private static final String VISIT_CUTOFF_TIME = "05:00";
    private final Database db;

    public VisitProcessor(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE, CORE);
    }

    public void insertVisits(String timeZone) {
        log.info("Visit calculation starting at {}", timeZone);
        var now = ZonedDateTime.now(ZoneId.of(timeZone));
        var endTime = now.with(LocalTime.parse(VISIT_CUTOFF_TIME));
        var startTime = endTime.minusDays(1);

        db.execute(sql -> {
            sql.insertInto(CORE.VISIT).
                    select(
                            (selectDistinct(RFID_DETECTION.CLIENT_ID, ASSIGNMENT.CLIENT_REFERENCE,
                                    RFID_DETECTION.DETECTION_TIME.minOver()
                                            .partitionBy(RFID_DETECTION.CLIENT_ID,
                                                    ANTENNA.SITE_ID, ASSIGNMENT.CLIENT_REFERENCE),
                                    RFID_DETECTION.DETECTION_TIME.maxOver()
                                            .partitionBy(RFID_DETECTION.CLIENT_ID,
                                                    ANTENNA.SITE_ID, ASSIGNMENT.CLIENT_REFERENCE),
                                    val("measured-day"),
                                    ANTENNA.SITE_ID)
                                    .from(CORE.RFID_DETECTION)
                                    .innerJoin(CORE.ANTENNA).on(ANTENNA.CLIENT_ID.eq(RFID_DETECTION.CLIENT_ID)
                                            .and(ANTENNA.READER_SN.eq(RFID_DETECTION.READER_SN)
                                                    .and(ANTENNA.PORT_NUMBER.eq(RFID_DETECTION.PORT_NUMBER))))
                                    .innerJoin(CORE.SITE).on(SITE.CLIENT_ID.eq(ANTENNA.CLIENT_ID).and(SITE.SITE_ID.eq(ANTENNA.SITE_ID)))
                                    .join(CORE.DETECTABLE).onKey(Keys.RFID_DETECTION__FK_RFID_DETECTION_TO_DETECTABLE)
                                    .join(CORE.ASSIGNMENT).onKey(Keys.ASSIGNMENT__FK_ASSIGNMENT_TO_DETECTABLE)
                                    .where(RFID_DETECTION.DETECTION_TIME.between(utcLocalFromZoned(startTime),
                                            utcLocalFromZoned(endTime)))
                                    .and(SITE.TIME_ZONE.eq(timeZone))
                                    .andExists(selectOne().from(CORE.CONTACT)
                                            .where(CORE.CONTACT.CLIENT_REFERENCE.eq(ASSIGNMENT.CLIENT_REFERENCE)
                                            )))
                    ).onConflictDoNothing().execute();
            log.info("Visit tuples inserted for {}: {}", timeZone);
            return null;
        });
    }
}
