package id.unifi.service.core.processing;

import id.unifi.service.core.CoreService;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.*;

public class VisitProcessor {
    private static final Logger log = LoggerFactory.getLogger(CoreService.class);
    private final Database db;

    public VisitProcessor(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
    }

    public void insertVisits(String timeZone) {
        log.info("Visit calculation starting at " + timeZone);

        var now = LocalDateTime.now();
        var detectedToday = getDetectedDetectablesOnDate(now);
        var countOfVisitsInserted = 0;

        for (String detectableId : detectedToday) {

            db.execute(sql -> sql.insertInto(CORE.VISIT).
                    select(
                            (DSL.select(RFID_DETECTION.CLIENT_ID, ASSIGNMENT.CLIENT_REFERENCE,
                                    RFID_DETECTION.DETECTION_TIME.minOver()
                                            .partitionBy(RFID_DETECTION.CLIENT_ID,
                                                    RFID_DETECTION.DETECTABLE_ID, RFID_DETECTION.READER_SN),
                                    RFID_DETECTION.DETECTION_TIME.maxOver()
                                            .partitionBy(RFID_DETECTION.CLIENT_ID,
                                                    RFID_DETECTION.DETECTABLE_ID, RFID_DETECTION.READER_SN),
                                    DSL.val("measured-day"),
                                    READER.SITE_ID
                            ).distinctOn(RFID_DETECTION.DETECTABLE_ID)
                                    .from(CORE.RFID_DETECTION)
                                    .join(CORE.READER).onKey()
                                    .join(CORE.ASSIGNMENT).onKey()
                                    .where(RFID_DETECTION.DETECTION_TIME.between(now.minusDays(1), now))
                                    .and(SITE.TIME_ZONE.eq(timeZone))
                                    .and(RFID_DETECTION.DETECTABLE_ID.eq(detectableId))
                                    .andExists(DSL.selectOne().from(CORE.CONTACT)
                                            .where(CORE.CONTACT.CLIENT_REFERENCE.eq(ASSIGNMENT.CLIENT_REFERENCE)
                                            )))


                    ).onConflictDoNothing().execute()
            );
            countOfVisitsInserted++;
        }
        log.info("Visit tuples inserted for {}: {}", timeZone, countOfVisitsInserted);
    }

    private List<String> getDetectedDetectablesOnDate(LocalDateTime date) {
        return db.execute(sql -> sql.selectDistinct()
                .from(CORE.RFID_DETECTION)
                .where(RFID_DETECTION.DETECTION_TIME.between(date.minusDays(1), date))
                .groupBy(RFID_DETECTION.DETECTABLE_ID)
                .having(RFID_DETECTION.DETECTION_TIME.count().gt(1))
                .fetch(RFID_DETECTION.DETECTABLE_ID));
    }
}
