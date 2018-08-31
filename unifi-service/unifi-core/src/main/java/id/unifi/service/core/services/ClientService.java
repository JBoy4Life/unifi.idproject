package id.unifi.service.core.services;

import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.annotations.HttpMatch;
import id.unifi.service.common.api.errors.NotFound;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.types.pk.OperatorPK;
import id.unifi.service.common.util.ContentTypeUtils.ImageWithType;
import static id.unifi.service.dbcommon.DatabaseUtils.fieldValueOpt;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.CLIENT;
import static id.unifi.service.core.db.Tables.CLIENT_IMAGE;
import static id.unifi.service.core.db.Tables.HOLDER_IMAGE;
import org.jooq.Record;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nullable;
import java.util.*;

@ApiService("client")
public class ClientService {
    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final Database db;

    public ClientService(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
    }

    @ApiOperation
    @HttpMatch(path = "clients")
    public List<ClientInfo> listClients(OperatorSessionData session, @Nullable Set<String> with) {
        log.info("Listing clients");
        authorize(session);
        return db.execute(sql -> sql.selectFrom(calculateTableJoin(with))

                .fetch(ClientService::clientInfoFromRecord));
    }

    // creating operators goes into the logs of the server
    @ApiOperation
    @HttpMatch(path = "clients/:clientId")
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
