package id.unifi.service.core.agentservices;

import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import static id.unifi.service.common.db.DatabaseProvider.CORE_SCHEMA_NAME;
import id.unifi.service.core.AgentSessionData;
import static id.unifi.service.core.db.Tables.CLIENT;
import static org.jooq.impl.DSL.selectFrom;

@ApiService("agent")
public class AgentService {
    private final Database db;

    public AgentService(DatabaseProvider dbProvider) {
        db = dbProvider.bySchemaName(CORE_SCHEMA_NAME);
    }

    @ApiOperation
    public void identify(AgentSessionData session, String clientId) {
        boolean exists = db.execute(sql -> sql.fetchExists(selectFrom(CLIENT).where(CLIENT.CLIENT_ID.eq(clientId))));
        if (!exists) {
            throw new RuntimeException("Client '" + clientId + "' doesn't exist");
        }

        session.setClientId(clientId);
    }
}
