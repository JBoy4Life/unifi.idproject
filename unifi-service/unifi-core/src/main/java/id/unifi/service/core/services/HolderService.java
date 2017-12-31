package id.unifi.service.core.services;

import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import static id.unifi.service.common.db.DatabaseProvider.CORE_SCHEMA_NAME;
import id.unifi.service.common.operator.OperatorSessionData;
import static id.unifi.service.core.db.Tables.HOLDER;

import java.util.List;

@ApiService("holder")
public class HolderService {
    private final Database db;

    public HolderService(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchemaName(CORE_SCHEMA_NAME);
    }

    @ApiOperation
    public List<HolderInfo> listHolders(OperatorSessionData session, String clientId) {
        return db.execute(sql -> sql.selectFrom(HOLDER)
                .where(HOLDER.CLIENT_ID.eq(clientId))
                .fetch(r -> new HolderInfo(r.getClientReference(), r.getName(), r.getHolderType(), r.getActive())));
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
