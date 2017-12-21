package id.unifi.service.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import id.unifi.service.common.api.Dispatcher;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import static id.unifi.service.common.db.DatabaseProvider.CORE_SCHEMA_NAME;
import id.unifi.service.common.rfid.RfidDetectionReport;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentHandler implements Dispatcher.SessionListener<AgentSessionData> {
    private static final Logger log = LoggerFactory.getLogger(AgentHandler.class);

    private final Database db;
    private final DetectionProcessor detectionProcessor;
    private final BiMap<String, Session> agentSessions;
    private final Dispatcher<AgentSessionData> dispatcher;

    public AgentHandler(DatabaseProvider dbProvider,
                        Dispatcher<AgentSessionData> agentDispatcher,
                        DetectionProcessor detectionProcessor) {
        this.db = dbProvider.bySchemaName(CORE_SCHEMA_NAME);
        this.detectionProcessor = detectionProcessor;
        this.agentSessions = Maps.synchronizedBiMap(HashBiMap.create());

        this.dispatcher = agentDispatcher;
        agentDispatcher.<RfidDetectionReport>putMessageListener("core.agent.detections", RfidDetectionReport.class,
                (session, report) -> {
            String[] ids = agentSessions.inverse().get(session).split(":", 2);
            String clientId = ids[0];
            String siteId = ids[1];

            detectionProcessor.process(clientId, siteId, report);
        });
    }

    public void onSessionCreated(Session session, AgentSessionData sessionData) {
        agentSessions.put(sessionData.getClientId() + ":" + sessionData.getSiteId(), session);
    }

    public void onSessionDropped(Session session) {
        agentSessions.inverse().remove(session);
    }
}
