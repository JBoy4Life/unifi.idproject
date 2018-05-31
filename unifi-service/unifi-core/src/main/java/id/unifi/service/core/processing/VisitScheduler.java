package id.unifi.service.core.processing;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.core.CoreService;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.Duration;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.*;
import static java.util.concurrent.TimeUnit.*;


public class VisitScheduler {

    private static final Logger log = LoggerFactory.getLogger(CoreService.class);

    private final ScheduledExecutorService scheduler;
    private final Database db;
    private final static int TIME_DIFF = 26;
    private final String detectableType = "uhf-epc";
    private final String visitClients = "ucl-som";
    private final String clientId = "centralworking";

    public VisitScheduler (DatabaseProvider dbProvider) {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.db = dbProvider.bySchema(CORE);
    }
    //FOR EACH CLIENT REF IN CLIENT REF
    public void insertVisits() {

        var desiredTime = LocalTime.of(5, 0);
        var now = LocalDateTime.now();
        var next = desiredTime.isAfter(now.toLocalTime()) ? now.with(desiredTime) : now.with(desiredTime).plusDays(1);
        var delay = Duration.between(now, next);
        var initDelay = delay.toHours();

        Runnable insertVisits = () -> {
            log.info("Inserting Visits");





            List<String> detectedToday = db.execute(sql -> sql.selectDistinct(RFID_DETECTION.DETECTABLE_ID)
                    .from(CORE.RFID_DETECTION)
                    .where(RFID_DETECTION.DETECTION_TIME.between(now, now.minusDays(1)).and(RFID_DETECTION.DETECTABLE_TYPE.eq(detectableType)))
                    .fetch(RFID_DETECTION.DETECTABLE_ID));

            for(String detectableID : detectedToday) {


                int checkInterpolatedMonth = db.execute(sql -> sql.selectCount()
                        .from(CORE.VISIT)
                        .innerJoin(ASSIGNMENT)
                        .on(VISIT.CLIENT_REFERENCE.eq(ASSIGNMENT.CLIENT_REFERENCE))
                        .innerJoin(CORE.CONTACT)
                        .on(VISIT.CLIENT_REFERENCE.eq(CONTACT.CLIENT_REFERENCE))
                        .innerJoin(CORE.CLIENT)
                        .on(VISIT.CLIENT_ID.eq(CLIENT.CLIENT_ID))
                        .where(ASSIGNMENT.DETECTABLE_ID.eq(detectableID).and(CORE.CONTACT.CLIENT_ID.eq(clientId)).and(VISIT.START_TIME.between(now, now.minusMonths(1)))).fetchOne(0, int.class));

                int checkInterpolatedSite = db.execute(sql -> sql.selectCount()
                        .from(CORE.VISIT)
                        .innerJoin(ASSIGNMENT)
                        .on(VISIT.CLIENT_REFERENCE.eq(ASSIGNMENT.CLIENT_REFERENCE))
                        .innerJoin(CORE.CONTACT)
                        .on(VISIT.CLIENT_REFERENCE.eq(CONTACT.CLIENT_REFERENCE))
                        .innerJoin(CORE.CLIENT)
                        .on(VISIT.CLIENT_ID.eq(CLIENT.CLIENT_ID))
                        .where((CORE.CONTACT.CLIENT_ID.eq(clientId)).and(VISIT.START_TIME.between(now, now.minusMonths(1)))).fetchOne(0, int.class));




                var noOfDetections = db.execute(sql -> sql.selectCount().from(CORE.RFID_DETECTION).where(RFID_DETECTION.DETECTABLE_ID.eq(detectableID)).fetchOne(0, int.class));

                if(noOfDetections > 1) {
                    //Measured Visits
                    db.execute(sql -> sql.insertInto(CORE.VISIT).
                            select(DSL.select(CONTACT.CLIENT_ID, HOLDER.CLIENT_REFERENCE,RFID_DETECTION.DETECTION_TIME.min(), RFID_DETECTION.DETECTION_TIME.max(), ASSIGNMENT.DETECTABLE_ID, DSL.val("measured-day"), SITE.SITE_ID)
                                    .from(CORE.ASSIGNMENT)
                                    .innerJoin(CORE.HOLDER).on(ASSIGNMENT.CLIENT_REFERENCE.eq(HOLDER.CLIENT_REFERENCE))
                                    .innerJoin(CORE.RFID_DETECTION).on(ASSIGNMENT.DETECTABLE_ID.eq(RFID_DETECTION.DETECTABLE_ID))
                                    .innerJoin(CORE.SITE).on(ASSIGNMENT.CLIENT_ID.eq(SITE.CLIENT_ID))
                                    .innerJoin(CORE.CONTACT).on(ASSIGNMENT.CLIENT_REFERENCE.eq(CONTACT.CLIENT_REFERENCE))
                                    .where(RFID_DETECTION.DETECTION_TIME.between(now, now.minusDays(1)))
                                    .and(RFID_DETECTION.DETECTABLE_ID.eq(detectableID))
                                    .groupBy(ASSIGNMENT.DETECTABLE_ID, HOLDER.CLIENT_REFERENCE, CONTACT.CLIENT_ID, SITE.SITE_ID)

                            ).execute()



                    );

                } else if(noOfDetections == 1) {


                    if(checkInterpolatedMonth > 7) {


                        db.execute(sql -> sql.insertInto(CORE.VISIT).
                                select(DSL.select(CONTACT.CLIENT_ID, HOLDER.CLIENT_REFERENCE, VISIT.START_TIME.avg(), VISIT.END_TIME.avg(), DSL.val("interpolated-day"), SITE.SITE_ID)
                                        .from(CORE.VISIT)
                                        .innerJoin(CORE.HOLDER).on(ASSIGNMENT.CLIENT_REFERENCE.eq(HOLDER.CLIENT_REFERENCE))
                                        .innerJoin(CORE.RFID_DETECTION).on(ASSIGNMENT.DETECTABLE_ID.eq(RFID_DETECTION.DETECTABLE_ID))
                                        .innerJoin(CORE.SITE).on(ASSIGNMENT.CLIENT_ID.eq(SITE.CLIENT_ID))
                                        .innerJoin(CORE.CONTACT).on(ASSIGNMENT.CLIENT_REFERENCE.eq(CONTACT.CLIENT_REFERENCE))
                                        .where(RFID_DETECTION.DETECTION_TIME.between(now, now.minusMonths(1)))
                                        .and(RFID_DETECTION.DETECTABLE_ID.eq(detectableID))
                                        .and(CONTACT.CLIENT_ID.eq(clientId))
                                        .groupBy(ASSIGNMENT.DETECTABLE_ID, HOLDER.CLIENT_REFERENCE, CONTACT.CLIENT_ID, SITE.SITE_ID)
                                ).execute()


                        );
                    } else if(checkInterpolatedSite > 30){
                        db.execute(sql -> sql.insertInto(CORE.VISIT).
                                select(DSL.select(CONTACT.CLIENT_ID, HOLDER.CLIENT_REFERENCE, VISIT.START_TIME.avg(), VISIT.END_TIME.avg(), DSL.val("interpolated-site"), SITE.SITE_ID)
                                        .from(CORE.VISIT)
                                        .innerJoin(CORE.HOLDER).on(ASSIGNMENT.CLIENT_REFERENCE.eq(HOLDER.CLIENT_REFERENCE))
                                        .innerJoin(CORE.RFID_DETECTION).on(ASSIGNMENT.DETECTABLE_ID.eq(RFID_DETECTION.DETECTABLE_ID))
                                        .innerJoin(CORE.SITE).on(ASSIGNMENT.CLIENT_ID.eq(SITE.CLIENT_ID))
                                        .innerJoin(CORE.CONTACT).on(ASSIGNMENT.CLIENT_REFERENCE.eq(CONTACT.CLIENT_REFERENCE))
                                        .where(RFID_DETECTION.DETECTION_TIME.between(now, now.minusMonths(1)))
                                        .and(RFID_DETECTION.DETECTABLE_ID.eq(detectableID))
                                        .and(CONTACT.CLIENT_ID.eq(clientId))
                                        .groupBy(ASSIGNMENT.DETECTABLE_ID, HOLDER.CLIENT_REFERENCE, CONTACT.CLIENT_ID, SITE.SITE_ID)
                                ).execute()


                        );
                    }

                }
            }

        };
            log.info("Thread will run after " + initDelay + " hours then 24hr intervals after that");


        var scheduledFuture =
                scheduler.scheduleAtFixedRate(insertVisits, initDelay, 24, HOURS );
    }
//PSEDUOCODE FOR InterpolatedDAY


    //IF NOT COUNT > 1
    //  IF MORE THAN 4 VISITS IN TOTAL ON THE SAME DAY WEEK? (QUESTION TIM ABOUT THIS)
    //      DURATION - AVG DURATION FROM PAST RESULTS


    //ELSE//

 //PSEDUOCODE FOR

}
