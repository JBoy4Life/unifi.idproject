package id.unifi.service.core.services;

import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.CLIENT;
import static java.util.stream.Collectors.toList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApiService("client")
public class ClientService {
    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final Database db;

    public ClientService(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
    }

    @ApiOperation
    public List<Client> listClients() {
        log.info("Listing clients");
        return db.execute(sql -> sql.selectFrom(CLIENT).fetch().stream()
                .map(c -> new Client(c.getClientId(), c.getDisplayName()))
                .collect(toList()));
    }

    private static class Client {
        public final String clientId;
        public final String displayName;

        Client(String clientId, String displayName) {
            this.clientId = clientId;
            this.displayName = displayName;
        }

        public String toString() {
            return "Client{" +
                    "clientId='" + clientId + '\'' +
                    ", displayName='" + displayName + '\'' +
                    '}';
        }
    }
}
