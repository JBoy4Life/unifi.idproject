package id.unifi.service.core.processing;

import com.coreoz.wisp.schedule.Schedules;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.core.CoreService;
import org.jooq.LoaderOptionsStep;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Time;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.*;
import com.coreoz.wisp.Scheduler;

public class VisitScheduler {

    private final static int VISIT_DAY_FROM = 1;
    enum TimeZone {

        UK("Europe/London"),
        KR("Asia/Seoul");
        //TODO: Possible rethink this structure, pull timezone from DB using Jooq and populate a list
        private final String timezone;

        TimeZone(final String timezone) {
            this.timezone = timezone;
        }

        public String getTimeZone() {
            return timezone;
        }

        public String timeZoneConversion(TimeZone region) {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm");

            ZoneId clientZoneId = ZoneId.of(region.getTimeZone());

            LocalDateTime visitCutOff = LocalDateTime.of(LocalDate.now(), LocalTime.of(5,0));
            
            ZonedDateTime clientDateTime = ZonedDateTime.of(visitCutOff, clientZoneId);
            ZonedDateTime utcDateTime = clientDateTime.withZoneSameInstant(ZoneId.of("UTC"));

            return format.format(utcDateTime);

        }

    }


    private static final Logger log = LoggerFactory.getLogger(CoreService.class);
    private static final Scheduler scheduler = new Scheduler();
    private final Database db;



    public VisitScheduler (DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
    }

    public void insertVisits(String timezone) {
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
                                    .and(SITE.TIME_ZONE.eq(timezone))
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

    public void visitSchedule() {
        log.info("INITIALIZING VISIT SCHEDULER");
        //TODO: Change to a for loop with List<TimeZone>
        TimeZone ukTimeZone = TimeZone.UK;
        TimeZone krTimeZone = TimeZone.KR;

        scheduler.schedule(
                () -> insertVisits(ukTimeZone.getTimeZone()),
                Schedules.executeAt(ukTimeZone.timeZoneConversion(ukTimeZone))
        );

        scheduler.schedule(
                () -> insertVisits(krTimeZone.getTimeZone()),
                Schedules.executeAt(krTimeZone.timeZoneConversion(krTimeZone))
        );
    }

}








