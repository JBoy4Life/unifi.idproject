package id.unifi.service.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import id.unifi.service.common.api.MessageListener;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.detection.Detection;
import id.unifi.service.common.detection.RawDetectionReport;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Keys.ASSIGNMENT__FK_ASSIGNMENT_TO_CARRIER;
import static id.unifi.service.core.db.Keys.DETECTABLE__FK_DETECTABLE_TO_CARRIER;
import static id.unifi.service.core.db.Tables.ANTENNA;
import static id.unifi.service.core.db.Tables.DETECTABLE;
import static id.unifi.service.core.db.Tables.CARRIER;
import static id.unifi.service.core.db.Tables.ASSIGNMENT;
import id.unifi.service.core.db.tables.records.AntennaRecord;
import id.unifi.service.core.site.ResolvedDetection;
import static java.util.Collections.list;
import static java.util.Collections.newSetFromMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import org.eclipse.jetty.websocket.api.Session;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

public class DefaultDetectionProcessor implements DetectionProcessor {
    private static final Logger log = LoggerFactory.getLogger(DefaultDetectionProcessor.class);

    private static final String DETECTIONS_MESSAGE_TYPE = "core.site.detection-report";

    private final Database db;
    private final LoadingCache<String, Set<ListenerWithSession>> detectionListeners;

    public DefaultDetectionProcessor(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
        this.detectionListeners = CacheBuilder.newBuilder()
                .build(CacheLoader.from((String k) -> newSetFromMap(new HashMap<ListenerWithSession, Boolean>())));
    }

    public void process(Detection detection) {
        String clientId = detection.detectable.clientId;
        Set<ListenerWithSession> listeners = detectionListeners.getIfPresent(clientId);
        if (listeners == null || listeners.isEmpty()) {
            log.trace("No client listening on detections for {}", clientId);
            return;
        }

        List<ResolvedDetection> resolvedDetections = db.execute(sql -> {
            String zoneId = sql.selectFrom(ANTENNA)
                    .where(ANTENNA.CLIENT_ID.eq(clientId))
                    .and(ANTENNA.READER_SN.eq(detection.readerSn))
                    .and(ANTENNA.PORT_NUMBER.eq(detection.portNumber))
                    .fetchOne(ANTENNA.ZONE_ID);
            if (zoneId == null) return Stream.<ResolvedDetection>empty();

            String clientReference = sql.selectFrom(DETECTABLE
                    .join(CARRIER).onKey(DETECTABLE__FK_DETECTABLE_TO_CARRIER)
                    .join(ASSIGNMENT).onKey(ASSIGNMENT__FK_ASSIGNMENT_TO_CARRIER))
                    .where(DETECTABLE.DETECTABLE_ID.eq(detection.detectable.detectableId))
                    .fetchOne(ASSIGNMENT.CLIENT_REFERENCE);
            if (clientReference == null) return Stream.<ResolvedDetection>empty();

            return Stream.of(new ResolvedDetection(detection.detectionTime, clientReference, zoneId));
        }).collect(toList());

        for (ListenerWithSession l : listeners) {
            log.trace("Sending {} detections for {} to {}", resolvedDetections.size(), clientId, l.session);
            l.listener.accept(DETECTIONS_MESSAGE_TYPE, resolvedDetections);
        }
    }

    public void addListener(String clientId,
                            String siteId,
                            Session session,
                            MessageListener<List<ResolvedDetection>> listener) {
        log.info("Adding listener for {}", clientId);
        detectionListeners.getUnchecked(clientId).add(new ListenerWithSession(session, listener));
    }

    private class ListenerWithSession {
        final Session session;
        final MessageListener<List<ResolvedDetection>> listener;

        ListenerWithSession(Session session, MessageListener<List<ResolvedDetection>> listener) {
            this.session = session;
            this.listener = listener;
        }
    }
}
