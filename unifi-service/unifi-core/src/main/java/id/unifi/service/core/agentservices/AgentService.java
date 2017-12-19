package id.unifi.service.core.agentservices;

import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import static id.unifi.service.common.db.DatabaseProvider.CORE_SCHEMA_NAME;
import id.unifi.service.core.AgentSessionData;
import static id.unifi.service.core.db.Tables.CLIENT;
import static org.jooq.impl.DSL.selectFrom;

import java.util.List;

@ApiService("agent")
public class AgentService {
    private final Database db;

    public AgentService(DatabaseProvider dbProvider) {
        db = dbProvider.bySchemaName(CORE_SCHEMA_NAME);
    }


}
