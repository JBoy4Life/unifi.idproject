package id.unifi.service.core.services;

import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.NotFound;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.types.pk.OperatorPK;
import id.unifi.service.common.util.QueryUtils.ImageWithType;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.*;

import static id.unifi.service.common.util.QueryUtils.fieldValueOpt;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.*;

@ApiService("client")
public class ClientService {
    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final Database db;

    public ClientService(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
    }

    @ApiOperation
    public List<ClientInfo> listClients(OperatorSessionData session, @Nullable Set<String> with) {
        log.info("Listing clients");
        authorize(session);
        return db.execute(sql -> sql.selectFrom(calculateTableJoin(with))

                .fetch(ClientService::clientInfoFromRecord));
    }


    @ApiOperation
    public void backDateMeasuredVisit(LocalDate startDate, LocalDate endDate, String clientId) {

        if (startDate.isAfter(endDate)) return;

        final String detectableType = "uhf-epc";



        //TODO: Change loop to go to endDate +1, make sure you check beforehand
        while (startDate.isBefore(endDate)) {
            var start = startDate.atTime(5 , 0);
            var end = start.plusDays(1);
            log.info(start.toString());
            log.info(end.toString());

            List<String> detectedToday = db.execute(sql -> sql.selectDistinct(RFID_DETECTION.DETECTABLE_ID)
                    .from(CORE.RFID_DETECTION)
                    .where(RFID_DETECTION.DETECTION_TIME.between(start, end)
                    .and(RFID_DETECTION.DETECTABLE_TYPE.eq(detectableType)))
                    .fetch(RFID_DETECTION.DETECTABLE_ID));
            for(String s : detectedToday) {

                log.info("DETECTED TODAY " + s);

            }
            Condition maxDuration = DSL.condition("age(visit.end_time, visit.start_time) < 23");

            for(String detectableID : detectedToday) {

                var noOfDetections = db.execute(sql -> sql.selectCount().from(CORE.RFID_DETECTION)
                        .where(RFID_DETECTION.DETECTABLE_ID
                                .eq(detectableID))
                        .and(RFID_DETECTION.DETECTION_TIME.between(start, end))
                        .fetchOne(0, int.class));
                if(noOfDetections == 1) {
                    log.info("SKIPPING");
                }
                if (noOfDetections > 1) {

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
                                            .where(RFID_DETECTION.DETECTION_TIME.between(start, end))
                                            .and(RFID_DETECTION.DETECTABLE_ID.eq(detectableID))
                                            .and(RFID_DETECTION.CLIENT_ID.eq(clientId))
                                            .and(RFID_DETECTION.DETECTION_TIME.notBetween(start, start.plusMinutes(30)))
                                            .and(RFID_DETECTION.DETECTION_TIME.notBetween(end.minusMinutes(30), end))
                                            .andExists(DSL.selectOne().from(CORE.CONTACT)
                                                    .where(CORE.CONTACT.CLIENT_REFERENCE.eq(ASSIGNMENT.CLIENT_REFERENCE)
                                                            .and(CORE.CONTACT.CLIENT_ID.eq(clientId)))))


                            ).onConflictDoNothing().execute()

                    );
                }

            }
            //Updates the current day
            startDate = startDate.plusDays(1);
        }

        //

    }

    @ApiOperation
    public void backDateInterpolatedVisit(LocalDate startDate, LocalDate endDate, String clientId) {
        if (startDate.isAfter(endDate)) return;

        final String detectableType = "uhf-epc";

        while (startDate.isBefore(endDate)) {
            var start = startDate.atTime(5 , 0);
            var end = start.plusDays(1);
            log.info(start.toString());
            log.info(end.toString());


            // System only detects uhf-epc so the check may be redundant
            List<String> detectedToday = db.execute(sql -> sql.selectDistinct(RFID_DETECTION.DETECTABLE_ID)
                    .from(CORE.RFID_DETECTION)
                    .where(RFID_DETECTION.DETECTION_TIME.between(start, end).and(RFID_DETECTION.DETECTABLE_TYPE.eq(detectableType)))
                    .fetch(RFID_DETECTION.DETECTABLE_ID));


            //Plain sql in JOOQ
            Field<?> AVG_START_TIME  = DSL.field("date'" + startDate.toString() + "'+ avg(visit.start_time::time)");
            Field<?> AVG_END_TIME  = DSL.field("date'" + startDate.toString() + "'+ avg(visit.end_time::time)");


            //For each detection seen within a 24 hr period (5am to 5am(next day))
            for (String detectableID : detectedToday) {

                var noOfDetections = db.execute(sql -> sql.selectCount().from(CORE.RFID_DETECTION)
                        .where(RFID_DETECTION.DETECTABLE_ID
                                .eq(detectableID))
                        .and(RFID_DETECTION.DETECTION_TIME.between(start, end))
                        .fetchOne(0, int.class));

                //If number of detections is equal to 1 then we must interpolate

                if (noOfDetections == 1) {
                    log.info("INTERPOLATING BASED OFF " + noOfDetections.toString() + " DETECTION(S)");

                    //check how many detections they have had in the past month on the same day

                    int checkInterpolatedDay = db.execute(sql -> sql.selectCount()
                            .from(CORE.VISIT)
                            .where(
                                    VISIT.START_TIME.between(start.minusWeeks(1), start.minusDays(6))
                                            .or(VISIT.START_TIME.between(start.minusWeeks(2), start.minusDays(13)))
                                            .or(VISIT.START_TIME.between(start.minusWeeks(3), start.minusDays(20)))
                                            .or(VISIT.START_TIME.between(start.minusWeeks(4), start.minusDays(27)))
                                            .and(VISIT.CLIENT_REFERENCE.eq(DSL.select(ASSIGNMENT.CLIENT_REFERENCE)
                                                    .from(CORE.ASSIGNMENT)
                                                    .where(ASSIGNMENT.DETECTABLE_ID.eq(detectableID))
                                            ))
                            )
                            .fetchOne(0, int.class));

                    //TODO: Only do checks for interpolated Month and Site iff interpolated day yields false

                    log.info("CHECK INTERPOLATED DAY = " + checkInterpolatedDay);


                    int checkInterpolatedMonth = db.execute(sql -> sql.selectCount()
                            .from(CORE.VISIT)
                            .where(VISIT.CLIENT_REFERENCE.eq(DSL.select(ASSIGNMENT.CLIENT_REFERENCE)
                                    .from(CORE.ASSIGNMENT)
                                    .where(ASSIGNMENT.DETECTABLE_ID.eq(detectableID)))
                                    .and(VISIT.START_TIME.between(start.minusMonths(1), start))
                            ).fetchOne(0, int.class));
                    log.info("CHECK INTERPOLATED MONTH = " + checkInterpolatedMonth);



                    if (checkInterpolatedDay > 3 ){
                        log.info("INTERPOLATING BASED OFF DAY");
                        db.execute(sql -> sql.insertInto(CORE.VISIT).
                                select(DSL.select(
                                        RFID_DETECTION.CLIENT_ID,
                                        ASSIGNMENT.CLIENT_REFERENCE,
                                        AVG_START_TIME,
                                        AVG_END_TIME,
                                        DSL.val("interpolated-day"),
                                        READER.SITE_ID)
                                        .from(CORE.VISIT)
                                        .innerJoin(CORE.RFID_DETECTION).on(VISIT.CLIENT_ID.eq(RFID_DETECTION.CLIENT_ID))
                                        .innerJoin(CORE.ASSIGNMENT).on(VISIT.CLIENT_REFERENCE.eq(ASSIGNMENT.CLIENT_REFERENCE))
                                        .innerJoin(CORE.READER).on(VISIT.SITE_ID.eq(READER.SITE_ID))
                                        .where(
                                                RFID_DETECTION.DETECTABLE_ID.eq(detectableID)
                                                        .and(RFID_DETECTION.DETECTION_TIME.between(start, end))
                                                        .and(ASSIGNMENT.DETECTABLE_ID.eq(RFID_DETECTION.DETECTABLE_ID))
                                                        .and(READER.READER_SN.eq(RFID_DETECTION.READER_SN))
                                                        .and(VISIT.START_TIME.between(start.minusWeeks(1), start.minusDays(6))
                                                                .or(VISIT.START_TIME.between(start.minusWeeks(2), start.minusDays(13)))
                                                                .or(VISIT.START_TIME.between(start.minusWeeks(3), start.minusDays(20)))
                                                                .or(VISIT.START_TIME.between(start.minusWeeks(4), start.minusDays(27))))

                                        ).groupBy(RFID_DETECTION.CLIENT_ID, ASSIGNMENT.CLIENT_REFERENCE, READER.SITE_ID)


                                ).execute()
                        );

                        log.info("INTERPOLATING BASED OFF DAY COMPLETE");
                    }
                    else if (checkInterpolatedMonth > 7) {
                        log.info("INTERPOLATING BASED OFF MONTH");
                        db.execute(sql -> sql.insertInto(CORE.VISIT).
                                select(DSL.select(
                                        RFID_DETECTION.CLIENT_ID,
                                        ASSIGNMENT.CLIENT_REFERENCE,
                                        AVG_START_TIME,
                                        AVG_END_TIME,
                                        DSL.val("interpolated-month"),
                                        READER.SITE_ID)
                                        .from(CORE.VISIT)
                                        .innerJoin(CORE.RFID_DETECTION).on(VISIT.CLIENT_ID.eq(RFID_DETECTION.CLIENT_ID))
                                        .innerJoin(CORE.ASSIGNMENT).on(VISIT.CLIENT_REFERENCE.eq(ASSIGNMENT.CLIENT_REFERENCE))
                                        .innerJoin(CORE.READER).on(VISIT.SITE_ID.eq(READER.SITE_ID))
                                        .where(
                                                 RFID_DETECTION.DETECTABLE_ID.eq(detectableID)
                                                .and(RFID_DETECTION.DETECTION_TIME.between(start, end))
                                                .and(ASSIGNMENT.DETECTABLE_ID.eq(RFID_DETECTION.DETECTABLE_ID))
                                                .and(READER.READER_SN.eq(RFID_DETECTION.READER_SN))
                                                .and(VISIT.START_TIME.between(start.minusMonths(1), start))
                                        ).groupBy(RFID_DETECTION.CLIENT_ID, ASSIGNMENT.CLIENT_REFERENCE, READER.SITE_ID)

                                        ).execute()
                        );
                        log.info("INTERPOLATING BASED OFF MONTH COMPLETE");
                    }
                }

            }
            startDate = startDate.plusDays(1);
        }


    }

    // creating operators goes into the logs of the server
    @ApiOperation
    public ClientInfo getClient(String clientId, @Nullable Set<String> with) {
        return db.execute(sql -> sql.selectFrom(calculateTableJoin(with))
                .where(CLIENT.CLIENT_ID.eq(clientId))
                .fetchOptional(ClientService::clientInfoFromRecord))
                .orElseThrow(() -> new NotFound("client"));
    }

    private static Table<? extends Record> calculateTableJoin(@Nullable Set<String> with) {
        if (with == null) with = Set.of();

        Table<? extends Record> tables = CLIENT;
        if (with.contains("image")) {
            tables = tables.leftJoin(CLIENT_IMAGE).onKey();
        }

        return tables;
    }


    private static ClientInfo clientInfoFromRecord(Record r) {
        return new ClientInfo(
                r.get(CLIENT.CLIENT_ID),
                r.get(CLIENT.DISPLAY_NAME),
                fieldValueOpt(r, CLIENT_IMAGE.IMAGE).map(i -> new ImageWithType(i, r.get(HOLDER_IMAGE.MIME_TYPE))));
    }


    private static OperatorPK authorize(OperatorSessionData sessionData) {
        return Optional.ofNullable(sessionData.getOperator())
                .orElseThrow(Unauthorized::new);
    }

    public static class ClientInfo {
        public final String clientId;
        public final String displayName;
        public final Optional<ImageWithType> image;

        ClientInfo(String clientId, String displayName, Optional<ImageWithType> image) {
            this.clientId = clientId;
            this.displayName = displayName;
            this.image = image;
        }

        public String toString() {
            return "ClientInfo{" +
                    "clientId='" + clientId + '\'' +
                    ", displayName='" + displayName + '\'' +
                    ", image=" + image +
                    '}';
        }
    }



    //

    public static class VisitReport {

        public final String clientReference;
        public final String firstName;
        public final String lastName;
        public final String homeSite;
        public final String memberType;
        public final String totalHours;
        public final HashMap<String, String> hoursBySite;

        VisitReport(String clientReference, String firstName, String lastName, String homeSite, String memberType, String totalHours, HashMap hoursBySite) {

            this.clientReference = clientReference;
            this.firstName = firstName;
            this.lastName = lastName;
            this.homeSite = homeSite;
            this.memberType = memberType;
            this.totalHours = totalHours;
            this.hoursBySite = hoursBySite;
        }



    }
}
