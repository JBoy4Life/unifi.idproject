package id.unifi.service.core.services;

import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.operator.OperatorPK;
import id.unifi.service.common.operator.OperatorSessionData;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.HOLDER;
import static id.unifi.service.core.db.Tables.HOLDER_METADATA;
import id.unifi.service.core.db.tables.records.HolderRecord;
import org.jooq.Record1;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.value;

import java.util.List;
import java.util.Optional;

@ApiService("holder")
public class HolderService {
    private final Database db;

    public HolderService(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
    }

    @ApiOperation
    public List<HolderInfo> listHolders(OperatorSessionData session, String clientId) {
        authorize(session, clientId);
        return db.execute(sql -> sql.selectFrom(HOLDER)
                .where(HOLDER.CLIENT_ID.eq(clientId))
                .fetch(HolderService::recordToInfo));
    }

    @ApiOperation
    public HolderInfo getHolder(OperatorSessionData session, String clientId, String clientReference) {
        authorize(session, clientId);
        return db.execute(sql -> sql.selectFrom(HOLDER)
                .where(HOLDER.CLIENT_ID.eq(clientId))
                .and(HOLDER.CLIENT_REFERENCE.eq(clientReference))
                .fetchOne(HolderService::recordToInfo));
    }

    @ApiOperation
    public List<String> listMetadataValues(OperatorSessionData session, String clientId, String key) {
        authorize(session, clientId);
        return db.execute(sql -> sql
                .selectDistinct(field("{0} ->> {1}", String.class, HOLDER_METADATA.METADATA, value(key)))
                .from(HOLDER.leftJoin(HOLDER_METADATA).onKey())
                .where(HOLDER.CLIENT_ID.eq(clientId))
                .fetch(Record1::value1));
    }


    private static HolderInfo recordToInfo(HolderRecord r) {
        return new HolderInfo(r.getClientReference(), r.getName(), r.getHolderType(), r.getActive());
    }

    private static OperatorPK authorize(OperatorSessionData sessionData, String clientId) {
        return Optional.ofNullable(sessionData.getOperator())
                .filter(op -> op.clientId.equals(clientId))
                .orElseThrow(Unauthorized::new);
    }

    public static class HolderInfo {
        public final String clientReference;
        public final String name;
        public final String holderType;
        public final boolean active;

        public HolderInfo(String clientReference, String name, String holderType, boolean active) {
            this.clientReference = clientReference;
            this.name = name;
            this.holderType = holderType;
            this.active = active;
        }
    }
}
