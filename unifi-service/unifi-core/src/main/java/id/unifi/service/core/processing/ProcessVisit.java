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

public class ProcessVisit {
    private final static int VISIT_DAY_FROM = 1;
    private static final Logger log = LoggerFactory.getLogger(CoreService.class);
    private final Database db;

    public ProcessVisit (DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
    }

    public void insertVisits(String timeZone) {
        log.info("VISIT CALCULATION STARTING");

        var now = LocalDateTime.now();
        var detectedToday = getDetectableIdsSeenToday(now);
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
                                    .innerJoin(CORE.READER).on(READER.READER_SN.eq(RFID_DETECTION.READER_SN))
                                    .innerJoin(CORE.ASSIGNMENT).on(ASSIGNMENT.DETECTABLE_ID.eq(RFID_DETECTION.DETECTABLE_ID))
                                    .where(RFID_DETECTION.DETECTION_TIME.between(now.minusDays(VISIT_DAY_FROM), now))
                                    .and(SITE.TIME_ZONE.eq(timeZone))
                                    .and(RFID_DETECTION.DETECTABLE_ID.eq(detectableId))
                                    .andExists(DSL.selectOne().from(CORE.CONTACT)
                                            .where(CORE.CONTACT.CLIENT_REFERENCE.eq(ASSIGNMENT.CLIENT_REFERENCE)
                                            )))


                    ).onConflictDoNothing().execute()
            );
            countOfVisitsInserted++;
        }
        log.info("VISITS TUPLES INSERTED " + countOfVisitsInserted);
    }

    public List<String> getDetectableIdsSeenToday(LocalDateTime now) {
        return db.execute(sql -> sql.selectDistinct(RFID_DETECTION.DETECTABLE_ID)
                .from(CORE.RFID_DETECTION)
                .where(RFID_DETECTION.DETECTION_TIME.between(now.minusDays(VISIT_DAY_FROM), now))
                .groupBy(RFID_DETECTION.DETECTABLE_ID)
                .having(RFID_DETECTION.DETECTION_TIME.count().gt(1))
                .fetch(RFID_DETECTION.DETECTABLE_ID));
    }

}
