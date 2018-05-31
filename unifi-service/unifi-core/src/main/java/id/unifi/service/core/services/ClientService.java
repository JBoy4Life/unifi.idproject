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
import id.unifi.service.core.db.tables.Zone;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZonedDateTime;
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


   /* public List<Visit> getVisits(OperatorSessionData session, LocalDateTime startTime, LocalDateTime endTime) {

        List<Visit>  allVisits =  db.execute(sql -> sql.select(ASSIGNMENT.DETECTABLE_ID,
                HOLDER.NAME,
                RFID_DETECTION.DETECTION_TIME.min(),
                RFID_DETECTION.DETECTION_TIME.max(),
                HOLDER.CLIENT_REFERENCE)
                .from(
                        ASSIGNMENT).
                        innerJoin(HOLDER)
                .on(ASSIGNMENT.CLIENT_REFERENCE.eq(HOLDER.CLIENT_REFERENCE))
                .innerJoin(RFID_DETECTION)
                .on(ASSIGNMENT.DETECTABLE_ID.eq(RFID_DETECTION.DETECTABLE_ID))
                .where(RFID_DETECTION.DETECTION_TIME.ge(startTime).and(RFID_DETECTION.DETECTION_TIME.le(endTime)))
                .groupBy(RFID_DETECTION.DETECTABLE_ID)
                .fetch(ClientService::visitInfoFromRecord)


        );
            return allVisits;

    }*/

   /*

    */
    @ApiOperation
    public void backDateMeasuredVisit(LocalDate startDate, LocalDate endDate, String clientId) {

        if (startDate.isAfter(endDate)) return;

        final String detectableType = "uhf-epc";

        while(startDate.isBefore(endDate)) {
            var start = startDate.atTime(5 , 0);
            var end = start.plusDays(1);
            log.info(start.toString());
            log.info(end.toString());

            List<String> detectedToday = db.execute(sql -> sql.selectDistinct(RFID_DETECTION.DETECTABLE_ID)
                    .from(CORE.RFID_DETECTION)
                    .where(RFID_DETECTION.DETECTION_TIME.between(start, end).and(RFID_DETECTION.DETECTABLE_TYPE.eq(detectableType)))
                    .fetch(RFID_DETECTION.DETECTABLE_ID));
            for(String s : detectedToday) {
                System.out.println("detected today --->" + s);
            }

            for(String detectableID : detectedToday) {

                var noOfDetections = db.execute(sql -> sql.selectCount().from(CORE.RFID_DETECTION).where(RFID_DETECTION.DETECTABLE_ID.eq(detectableID)).fetchOne(0, int.class));

                System.out.println(noOfDetections);
                if (noOfDetections > 1) {
                    //Measured Visits
                    db.execute(sql -> sql.insertInto(CORE.VISIT).
                            select(DSL.select(CONTACT.CLIENT_ID, HOLDER.CLIENT_REFERENCE, RFID_DETECTION.DETECTION_TIME.min(), RFID_DETECTION.DETECTION_TIME.max(), DSL.val("measured-day"), SITE.SITE_ID)
                                    .from(CORE.ASSIGNMENT)
                                    .innerJoin(CORE.HOLDER).on(ASSIGNMENT.CLIENT_REFERENCE.eq(HOLDER.CLIENT_REFERENCE))
                                    .innerJoin(CORE.RFID_DETECTION).on(ASSIGNMENT.DETECTABLE_ID.eq(RFID_DETECTION.DETECTABLE_ID))
                                    .innerJoin(CORE.SITE).on(ASSIGNMENT.CLIENT_ID.eq(SITE.CLIENT_ID))
                                    .innerJoin(CORE.CONTACT).on(ASSIGNMENT.CLIENT_REFERENCE.eq(CONTACT.CLIENT_REFERENCE))
                                    .where(RFID_DETECTION.DETECTION_TIME.between(start, end))
                                    .and(RFID_DETECTION.DETECTABLE_ID.eq(detectableID))
                                    .and(CONTACT.CLIENT_ID.eq(clientId))
                                    .groupBy(ASSIGNMENT.DETECTABLE_ID, HOLDER.CLIENT_REFERENCE, CONTACT.CLIENT_ID, SITE.SITE_ID)
                            ).execute()

                    );
                }

            }
            startDate = startDate.plusDays(1);
        }



    }

    @ApiOperation
    public void backDateInterpolatedVisits(LocalDate startDate, LocalDate endDate, String clientId) {
        if (startDate.isAfter(endDate)) return;

        final String detectableType = "uhf-epc";

        while(startDate.isBefore(endDate)) {
            var start = startDate.atTime(5 , 0);
            var end = start.plusDays(1);
            log.info(start.toString());
            log.info(end.toString());

            List<String> detectedToday = db.execute(sql -> sql.selectDistinct(RFID_DETECTION.DETECTABLE_ID)
                    .from(CORE.RFID_DETECTION)
                    .where(RFID_DETECTION.DETECTION_TIME.between(start, end).and(RFID_DETECTION.DETECTABLE_TYPE.eq(detectableType)))
                    .fetch(RFID_DETECTION.DETECTABLE_ID));
            for(String s : detectedToday) {
                System.out.println("detected today --->" + s);
            }

            for(String detectableID : detectedToday) {

                var noOfDetections = db.execute(sql -> sql.selectCount().from(CORE.RFID_DETECTION).where(RFID_DETECTION.DETECTABLE_ID.eq(detectableID)).fetchOne(0, int.class));

                System.out.println(noOfDetections);
                if (noOfDetections == 1) {

                    int checkInterpolatedMonth = db.execute(sql -> sql.selectCount()
                            .from(CORE.VISIT)
                            .innerJoin(ASSIGNMENT)
                            .on(VISIT.CLIENT_REFERENCE.eq(ASSIGNMENT.CLIENT_REFERENCE))
                            .innerJoin(CORE.CONTACT)
                            .on(VISIT.CLIENT_REFERENCE.eq(CONTACT.CLIENT_REFERENCE))
                            .innerJoin(CORE.CLIENT)
                            .on(VISIT.CLIENT_ID.eq(CLIENT.CLIENT_ID))
                            .where(ASSIGNMENT.DETECTABLE_ID.eq(detectableID).and(CORE.CONTACT.CLIENT_ID.eq(clientId)).and(VISIT.START_TIME.between(start, start.minusMonths(1)))).fetchOne(0, int.class));

                    int checkInterpolatedSite = db.execute(sql -> sql.selectCount()
                            .from(CORE.VISIT)
                            .innerJoin(ASSIGNMENT)
                            .on(VISIT.CLIENT_REFERENCE.eq(ASSIGNMENT.CLIENT_REFERENCE))
                            .innerJoin(CORE.CONTACT)
                            .on(VISIT.CLIENT_REFERENCE.eq(CONTACT.CLIENT_REFERENCE))
                            .innerJoin(CORE.CLIENT)
                            .on(VISIT.CLIENT_ID.eq(CLIENT.CLIENT_ID))
                            .where((CORE.CONTACT.CLIENT_ID.eq(clientId)).and(VISIT.START_TIME.between(start, start.minusMonths(1)))).fetchOne(0, int.class));

                    if(checkInterpolatedMonth > 7) {
                       db.execute(sql -> sql.insertInto(CORE.VISIT).
                               select(DSL.select(CONTACT.CLIENT_ID, HOLDER.CLIENT_REFERENCE, VISIT.START_TIME.avg(), VISIT.END_TIME.avg(), DSL.val("interpolated-day"), SITE.SITE_ID)
                                       .from(CORE.VISIT)
                                       .innerJoin(CORE.HOLDER).on(ASSIGNMENT.CLIENT_REFERENCE.eq(HOLDER.CLIENT_REFERENCE))
                                       .innerJoin(CORE.RFID_DETECTION).on(ASSIGNMENT.DETECTABLE_ID.eq(RFID_DETECTION.DETECTABLE_ID))
                                       .innerJoin(CORE.SITE).on(ASSIGNMENT.CLIENT_ID.eq(SITE.CLIENT_ID))
                                       .innerJoin(CORE.CONTACT).on(ASSIGNMENT.CLIENT_REFERENCE.eq(CONTACT.CLIENT_REFERENCE))
                                       .where(RFID_DETECTION.DETECTION_TIME.between(start, start.minusMonths(1)))
                                       .and(RFID_DETECTION.DETECTABLE_ID.eq(detectableID))
                                       .and(CONTACT.CLIENT_ID.eq(clientId))
                                       .groupBy(ASSIGNMENT.DETECTABLE_ID, HOLDER.CLIENT_REFERENCE, CONTACT.CLIENT_ID, SITE.SITE_ID)
                               ).execute()


                       );
                   } else if(checkInterpolatedSite > 30){
                       db.execute(sql -> sql.insertInto(CORE.VISIT).
                               select(DSL.select(CONTACT.CLIENT_ID, HOLDER.CLIENT_REFERENCE, VISIT.START_TIME.avg(), VISIT.END_TIME.avg(), DSL.val("interpolated-day"), SITE.SITE_ID)
                                       .from(CORE.VISIT)
                                       .innerJoin(CORE.HOLDER).on(ASSIGNMENT.CLIENT_REFERENCE.eq(HOLDER.CLIENT_REFERENCE))
                                       .innerJoin(CORE.RFID_DETECTION).on(ASSIGNMENT.DETECTABLE_ID.eq(RFID_DETECTION.DETECTABLE_ID))
                                       .innerJoin(CORE.SITE).on(ASSIGNMENT.CLIENT_ID.eq(SITE.CLIENT_ID))
                                       .innerJoin(CORE.CONTACT).on(ASSIGNMENT.CLIENT_REFERENCE.eq(CONTACT.CLIENT_REFERENCE))
                                       .where(RFID_DETECTION.DETECTION_TIME.between(start, start.minusMonths(1)))
                                       .and(RFID_DETECTION.DETECTABLE_ID.eq(detectableID))
                                       .and(CONTACT.CLIENT_ID.eq(clientId))
                                       .groupBy(ASSIGNMENT.DETECTABLE_ID, HOLDER.CLIENT_REFERENCE, CONTACT.CLIENT_ID, SITE.SITE_ID)
                               ).execute()


                       );
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

    //above Operation will return List<SiteDetectionReport> instead of a single instance
    //Map Structure for sites
    private static ClientInfo clientInfoFromRecord(Record r) {
        return new ClientInfo(
                r.get(CLIENT.CLIENT_ID),
                r.get(CLIENT.DISPLAY_NAME),
                fieldValueOpt(r, CLIENT_IMAGE.IMAGE).map(i -> new ImageWithType(i, r.get(HOLDER_IMAGE.MIME_TYPE))));
    }

    /* private static Visit visitInfoFromRecord(Record r) {
        return new Visit(
                r.get(VISIT.CLIENT_ID),
                r.get(VISIT.CLIENT_REFERENCE),
                r.get(VISIT.START_TIME),
                r.get(VISIT.END_TIME),
                r.get(VISIT.CALCULATION_METHOD),
                r.get(VISIT.SITE_ID)

        );
    } */

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
        public final String totalHours;
        public final String siteId;

        VisitReport(String firstName, String lastName, String totalHours, String clientReference, String calculationMethod, String siteId) {

            this.clientReference = clientReference;
            this.firstName = firstName;
            this.lastName = lastName;
            this.totalHours = totalHours;
            this.siteId = siteId;
        }



    }
}
