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

}
