package id.unifi.service.core.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HostAndPort;
import id.unifi.service.common.agent.AgentFullConfig;
import id.unifi.service.common.agent.ReaderFullConfig;
import id.unifi.service.common.api.Dispatcher;
import id.unifi.service.common.api.Protocol;
import static id.unifi.service.common.api.SerializationUtils.getObjectMapper;
import static id.unifi.service.common.api.Validation.shortId;
import static id.unifi.service.common.api.Validation.v;
import static id.unifi.service.common.api.Validation.validateAll;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.AuthenticationFailed;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import id.unifi.service.common.security.SecretHashing;
import id.unifi.service.core.AgentSessionData;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.AGENT;
import static id.unifi.service.core.db.Tables.AGENT_PASSWORD;
import static id.unifi.service.core.db.Tables.ANTENNA;
import static id.unifi.service.core.db.Tables.READER;
import id.unifi.service.core.db.tables.records.AgentRecord;
import id.unifi.service.core.db.tables.records.AntennaRecord;
import id.unifi.service.core.db.tables.records.ReaderRecord;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import org.eclipse.jetty.websocket.api.Session;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

        pushAgentConfig(session, sessionData);
    }

    private void pushAgentConfig(Session session, AgentSessionData sessionData) {
        var agent = sessionData.getAgent();
        var agentFullConfig = db.execute(sql -> {
            var rawAgentConfig = sql.selectFrom(AGENT)
                    .where(AGENT.CLIENT_ID.eq(agent.clientId))
                    .and(AGENT.AGENT_ID.eq(agent.agentId))
                    .fetchOptional(AgentRecord::getConfig);

            var readerRecords = sql.selectFrom(READER)
                    .where(READER.CLIENT_ID.eq(agent.clientId))
                    .and(READER.AGENT_ID.eq(agent.agentId))
                    .fetch();

            var antennae = sql.selectFrom(ANTENNA)
                    .where(ANTENNA.CLIENT_ID.eq(agent.clientId))
                    .and(ANTENNA.READER_SN.in(readerRecords.stream()
                            .map(ReaderRecord::getReaderSn)
                            .toArray(String[]::new)))
                    .and(ANTENNA.ACTIVE)
                    .stream()
                    .collect(groupingBy(AntennaRecord::getReaderSn));

            var readerConfigs = readerRecords.stream()
                    .map(r -> new ReaderFullConfig<>(
                            Optional.of(r.getReaderSn()),
                            Optional.of(HostAndPort.fromString(r.getEndpoint())),
                            Optional.of(splicePortConfig(r.getConfig(),
                                    antennae.get(r.getReaderSn()).stream().map(AntennaRecord::getPortNumber).collect(toSet())))))
                    .collect(toList());
            return new AgentFullConfig<>(rawAgentConfig, readerConfigs);
        });

            agentDispatcher.request(
                    session, Protocol.MSGPACK, "core.config.set-agent-config", Map.of("config", agentFullConfig));
    }

    private JsonNode splicePortConfig(JsonNode configNode, Set<Integer> enabledPortNumbers) {
        var objectMapper = getObjectMapper(Protocol.JSON);
        var portsNode = configNode.get("ports");
        var configuredPortNumbers = portsNode == null ? Set.of() :
                stream(spliteratorUnknownSize(portsNode.fieldNames(), ORDERED), false)
                        .map(Integer::parseInt)
                        .collect(toSet());

        var newPortsNode = objectMapper.createObjectNode();
        for (int portNumber : enabledPortNumbers) {
            var portNumberString = Integer.toString(portNumber);
            var newPortNode = configuredPortNumbers.contains(portNumber)
                    ? portsNode.get(portNumberString)
                    : objectMapper.createObjectNode();
            newPortsNode.set(portNumberString, newPortNode);
        }

        ObjectNode newConfigNode = configNode.deepCopy();
        newConfigNode.set("ports", newPortsNode);

        return newConfigNode;
    }

    private boolean passwordMatches(String clientId, String agentId, byte[] password) {
        var encodedHash = db.execute(sql -> sqlPasswordHash(sql, clientId, agentId));
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
