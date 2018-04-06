package id.unifi.service.core.agents;

import com.google.common.net.HostAndPort;
import id.unifi.service.common.api.Dispatcher;
import id.unifi.service.common.api.Protocol;
import static id.unifi.service.common.api.Validation.shortId;
import static id.unifi.service.common.api.Validation.v;
import static id.unifi.service.common.api.Validation.validateAll;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.AuthenticationFailed;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.detection.ReaderConfig;
import id.unifi.service.common.security.SecretHashing;
import id.unifi.service.core.AgentPK;
import id.unifi.service.core.AgentSessionData;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.AGENT_PASSWORD;
import static id.unifi.service.core.db.Tables.ANTENNA;
import static id.unifi.service.core.db.Tables.READER;
import id.unifi.service.core.db.tables.records.AntennaRecord;
import id.unifi.service.core.db.tables.records.ReaderRecord;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import org.eclipse.jetty.websocket.api.Session;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApiService("identity")
public class IdentityService {
    private static final Logger log = LoggerFactory.getLogger(IdentityService.class);

    private final Database db;
    private Dispatcher<AgentSessionData> agentDispatcher;

    public IdentityService(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
    }

    public void setAgentDispatcher(Dispatcher<AgentSessionData> agentDispatcher) {
        this.agentDispatcher = agentDispatcher;
    }

    @ApiOperation
    public void authPassword(Session session,
                             AgentSessionData sessionData,
                             String clientId,
                             String agentId,
                             byte[] password) {
        validateAll(
                v(shortId(clientId), AuthenticationFailed::new),
                v(shortId(agentId), AuthenticationFailed::new)
        );

        if (passwordMatches(clientId, agentId, password)) {
            log.info("Authentication success for agent {}:{}", clientId, agentId);
            sessionData.setAgent(clientId, agentId);
        } else {
            log.info("Authentication failure for agent {}:{}", clientId, agentId);
            sessionData.setAgent(null, null);
            throw new AuthenticationFailed();
        }

        pushReaderConfig(session, sessionData);
    }

    private void pushReaderConfig(Session session, AgentSessionData sessionData) {
        AgentPK agent = sessionData.getAgent();
        List<ReaderConfig> readerConfigs = db.execute(sql -> {
            List<ReaderRecord> readerRecords = sql.selectFrom(READER)
                    .where(READER.CLIENT_ID.eq(agent.clientId))
                    .and(READER.AGENT_ID.eq(agent.agentId))
                    .fetch();

            Map<String, List<AntennaRecord>> antennae = sql.selectFrom(ANTENNA)
                    .where(ANTENNA.CLIENT_ID.eq(agent.clientId))
                    .and(ANTENNA.READER_SN.in(readerRecords.stream()
                            .map(ReaderRecord::getReaderSn)
                            .toArray(String[]::new)))
                    .and(ANTENNA.ACTIVE)
                    .stream()
                    .collect(groupingBy(AntennaRecord::getReaderSn));

            return readerRecords.stream()
                    .map(r -> new ReaderConfig(
                            r.getReaderSn(),
                            HostAndPort.fromString(r.getEndpoint()),
                            antennae.get(r.getReaderSn()).stream().mapToInt(AntennaRecord::getPortNumber).toArray()))
                    .collect(toList());
        });

        try {
            agentDispatcher.request(
                    session,
                    Protocol.MSGPACK,
                    "core.config.set-reader-config",
                    Map.of("readers", readerConfigs));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean passwordMatches(String clientId, String agentId, byte[] password) {
        Optional<byte[]> encodedHash = db.execute(sql -> sqlPasswordHash(sql, clientId, agentId));
        return encodedHash.map(hash -> SecretHashing.check(password, hash)).orElse(false);

    }

    private static Optional<byte[]> sqlPasswordHash(DSLContext sql, String clientId, String agentId) {
        return sql.select(AGENT_PASSWORD.PASSWORD_HASH)
                .from(AGENT_PASSWORD)
                .where(AGENT_PASSWORD.CLIENT_ID.eq(clientId))
                .and(AGENT_PASSWORD.AGENT_ID.eq(agentId))
                .fetchOptional(AGENT_PASSWORD.PASSWORD_HASH);
    }
}
