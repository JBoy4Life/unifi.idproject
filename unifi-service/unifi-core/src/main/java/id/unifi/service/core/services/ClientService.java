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
import org.jooq.Record;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @ApiOperation
    public SiteDetectionReport siteDetectionsReports(OperatorSessionData session, ZonedDateTime startDate,ZonedDateTime endDate) {
        return new SiteDetectionReport("testName", "testCardNumber", 6.2, "real", "Paddington");
    }
    //above Operation will return List<SiteDetectionReport> instead of a single instance
    //Map Structure for sites
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

    public static class SiteDetectionReport {
        public final String cardNumber;
        public final String firstName;
        public final double totalHours;
        public final String homeSite;
        public final String memberType;
        SiteDetectionReport(String firstName, String cardNumber, double totalHours, String memberType, String homeSite) {
            this.firstName = firstName;
            this.cardNumber = cardNumber;
            this.totalHours = totalHours;
            this.memberType = memberType;
            this.homeSite = homeSite;
        }


    }
}
