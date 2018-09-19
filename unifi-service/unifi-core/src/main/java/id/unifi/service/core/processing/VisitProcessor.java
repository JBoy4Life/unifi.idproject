package id.unifi.service.core.processing;

import id.unifi.service.core.CoreService;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static id.unifi.service.common.util.TimeUtils.utcLocalFromZoned;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Keys.ASSIGNMENT__FK_ASSIGNMENT_TO_DETECTABLE;
import static id.unifi.service.core.db.Keys.READER__FK_READER_TO_SITE;
import static id.unifi.service.core.db.Keys.RFID_DETECTION__FK_RFID_DETECTION_TO_DETECTABLE;
import static id.unifi.service.core.db.Tables.*;
import static org.jooq.impl.DSL.*;

public class VisitProcessor {
    private static final Logger log = LoggerFactory.getLogger(CoreService.class);
    private static final String VISIT_CUTOFF_TIME = "05:00";
    private final Database db;

    public VisitProcessor(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
    }

    public void insertVisits(String timeZone) {
        log.info("Visit calculation starting for {}", timeZone);
        var now = ZonedDateTime.now(ZoneId.of(timeZone));
        var endTime = now.with(LocalTime.parse(VISIT_CUTOFF_TIME));
        var startTime = endTime.minusDays(1);

        log.info("Period for visit calculation starts at:{} ends at:{}", startTime, endTime);

        var inserted = db.execute(sql -> sql.insertInto(CORE.VISIT).
                 select(
                         (selectDistinct(RFID_DETECTION.CLIENT_ID, ASSIGNMENT.CLIENT_REFERENCE,
                                 RFID_DETECTION.DETECTION_TIME.minOver()
                                         .partitionBy(RFID_DETECTION.CLIENT_ID,
                                                 READER.SITE_ID, ASSIGNMENT.CLIENT_REFERENCE),
                                 RFID_DETECTION.DETECTION_TIME.maxOver()
                                         .partitionBy(RFID_DETECTION.CLIENT_ID,
                                                 READER.SITE_ID, ASSIGNMENT.CLIENT_REFERENCE),
                                 val("measured-day"), READER.SITE_ID)
                                 .from(RFID_DETECTION)
                                 .join(READER).on(READER.CLIENT_ID.eq(RFID_DETECTION.CLIENT_ID),
                                         READER.READER_SN.eq(RFID_DETECTION.READER_SN))
                                 .join(SITE).onKey(READER__FK_READER_TO_SITE)
                                 .join(CLIENT_CONFIG).on(RFID_DETECTION.CLIENT_ID.eq(CLIENT_CONFIG.CLIENT_ID))
                                 .join(DETECTABLE).onKey(RFID_DETECTION__FK_RFID_DETECTION_TO_DETECTABLE)
                                 .join(ASSIGNMENT).onKey(ASSIGNMENT__FK_ASSIGNMENT_TO_DETECTABLE)
                                 .where(RFID_DETECTION.DETECTION_TIME
                                         .between(utcLocalFromZoned(startTime), utcLocalFromZoned(endTime)))
                                 .and(SITE.TIME_ZONE.eq(timeZone))
                                 .and(CLIENT_CONFIG.VISIT_CALCULATION_ENABLED.isTrue())
                                 .andExists(selectOne().from(CONTACT)
                                         .where(CONTACT.CLIENT_REFERENCE.eq(ASSIGNMENT.CLIENT_REFERENCE)
                                         )))
                 ).onConflictDoNothing().execute());
        log.info("Visit tuples inserted for {}: {}", timeZone, inserted);
    }
}
