package id.unifi.service.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import id.unifi.service.common.api.MessageListener;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
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
import static java.util.Collections.newSetFromMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                .weakValues()
                .build(CacheLoader.from((String k) -> newSetFromMap(new WeakHashMap<ListenerWithSession, Boolean>())));
    }

    public void process(String clientId, String siteId, RawDetectionReport report) {
        String scopedSiteId = clientId + ":" + siteId;
        Set<String> epcCodes = report.detections.stream().map(d -> d.detectableId).collect(toSet());
        List<ResolvedDetection> resolvedDetections = db.execute(sql -> {
            Map<Integer, String> zoneMap = sql.selectFrom(ANTENNA)
                    .where(ANTENNA.CLIENT_ID.eq(clientId))
                    .and(ANTENNA.SITE_ID.eq(siteId))
                    .and(ANTENNA.READER_SN.eq(report.readerSn))
                    .stream()
                    .collect(toMap(AntennaRecord::getPortNumber, AntennaRecord::getZoneId));
            Map<String, String> assignments = sql.selectFrom(DETECTABLE
                    .innerJoin(CARRIER).onKey(DETECTABLE__FK_DETECTABLE_TO_CARRIER)
                    .innerJoin(ASSIGNMENT).onKey(ASSIGNMENT__FK_ASSIGNMENT_TO_CARRIER))
                    .where(DETECTABLE.DETECTABLE_ID.in(epcCodes))
                    .stream()
                    .collect(toMap(r -> r.get(DETECTABLE.DETECTABLE_ID), r -> r.get(ASSIGNMENT.CLIENT_REFERENCE)));

            return report.detections.stream()
                    .flatMap(d -> {
                        String clientReference = assignments.get(d.detectableId);
                        String zoneId = zoneMap.get(d.portNumber);
                        return clientReference != null && zoneId != null
                                ? Stream.of(new ResolvedDetection(d.timestamp, clientReference, zoneId))
                                : Stream.empty();
                    })
                    .collect(toList());
        });

        Set<ListenerWithSession> ls = detectionListeners.getIfPresent(scopedSiteId);
        if (ls == null || ls.isEmpty()) {
            log.trace("No client listening on detections at {}", scopedSiteId);
        } else {
            for (ListenerWithSession l : ls) {
                log.trace("Sending {} detections at {} to {}", resolvedDetections.size(), scopedSiteId, l.session);
                l.listener.accept(DETECTIONS_MESSAGE_TYPE, resolvedDetections);
            }
        }
    }

    public void addListener(String clientId,
                            String siteId,
                            Session session,
                            MessageListener<List<ResolvedDetection>> listener) {
        detectionListeners.getUnchecked(clientId + ":" + siteId).add(new ListenerWithSession(session, listener));
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
