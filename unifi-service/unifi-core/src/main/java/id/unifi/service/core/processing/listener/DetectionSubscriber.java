package id.unifi.service.core.processing.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Ordering;
import id.unifi.service.common.api.MessageListener;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.detection.Detection;
import id.unifi.service.common.detection.DetectionMatch;
import id.unifi.service.common.detection.DetectionMatchListener;
import id.unifi.service.common.subscriptions.SubscriptionHandler;
import id.unifi.service.common.subscriptions.SubscriptionManager;
import id.unifi.service.common.subscriptions.Topic;
import id.unifi.service.common.types.pk.DetectablePK;
import id.unifi.service.common.types.pk.SitePK;
import id.unifi.service.common.types.pk.ZonePK;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.CLIENT_CONFIG;
import static id.unifi.service.core.db.Tables.RFID_DETECTION;
import id.unifi.service.core.processing.DetectionMatcher;
import id.unifi.service.core.site.ResolvedSiteDetection;
import id.unifi.service.core.site.SiteDetectionSubscriptionType;
import id.unifi.service.core.site.ZoneDetectionSubscriptionType;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import static java.time.Instant.now;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toUnmodifiableList;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DetectionSubscriber implements DetectionMatchListener {
    private static final Logger log = LoggerFactory.getLogger(DetectionSubscriber.class);

    private static final String SUBSCRIBE_DETECTIONS_RESULT = "core.site.subscribe-detections-result";
    private static final String SUBSCRIBE_ZONE_DETECTIONS_RESULT = "core.site.subscribe-zone-detections-result";
    private static final Duration LAST_KNOWN_DETECTIONS_CUTOFF = Duration.ofDays(1);
    private static final Duration LAST_KNOWN_DETECTIONS_PRELOAD_TIMEOUT = Duration.ofMinutes(1);

    private final Cache<DetectablePK, Boolean> recentDetectables;
    private final SubscriptionManager subscriptionManager;
    private final Map<SiteAndClientReference, String> clientReferenceZones;
    private final Map<ZonePK, Map<String, Instant>> zoneClientReferenceTimes;
    private final CountDownLatch lastKnownPreloaded;

    private DetectionSubscriber(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
        this.recentDetectables = CacheBuilder.newBuilder().expireAfterWrite(1, SECONDS).build();
        this.clientReferenceZones = new ConcurrentHashMap<>();
        this.zoneClientReferenceTimes = new ConcurrentHashMap<>();
        this.lastKnownPreloaded = new CountDownLatch(1);
    }

    public static DetectionSubscriber create(SubscriptionManager subscriptionManager,
                                             DetectionMatcher detectionMatcher,
                                             DatabaseProvider dbProvider) {
        var subscriber = new DetectionSubscriber(subscriptionManager);
        subscriber.start(detectionMatcher, dbProvider);
        return subscriber;
    }

    private void start(DetectionMatcher detectionMatcher, DatabaseProvider dbProvider) {
        new Thread(() -> this.loadLastKnown(detectionMatcher, dbProvider.bySchema(CORE))).start();
    }

    public void accept(List<DetectionMatch> matches) {
        for (var match : matches) {
            var detectable = match.detection.detectable;

            // FIXME: Limit by detection time, not wall clock
            if (recentDetectables.getIfPresent(detectable) != null) continue;
            recentDetectables.put(detectable, true);

            match.clientReference.ifPresent(clientReference -> {
                var resolved =
                        new ResolvedSiteDetection(match.detection.detectionTime, clientReference, match.zone.zoneId);
                var detections = List.of(resolved);
                subscriptionManager.distributeMessage(
                        SiteDetectionSubscriptionType.topic(match.zone.getSite()), detections);
                subscriptionManager.distributeMessage(
                        ZoneDetectionSubscriptionType.topic(match.zone), detections);
            });

            updateSnapshot(match);
        }
    }

    public void addListener(SitePK site,
                            MessageListener<List<ResolvedSiteDetection>> listener,
                            boolean includeLastKnown) {
        log.debug("Adding listener for {}, includeLastKnown: {}", site, includeLastKnown);
        processSubscription(
                () -> zoneClientReferenceTimes.entrySet().stream().filter(e -> e.getKey().getSite().equals(site)),
                SiteDetectionSubscriptionType.topic(site), SUBSCRIBE_DETECTIONS_RESULT, listener, includeLastKnown);
    }

    public void addListener(ZonePK zone,
                            MessageListener<List<ResolvedSiteDetection>> listener,
                            boolean includeLastKnown) {
        log.debug("Adding listener for {}, includeLastKnown: {}", zone, includeLastKnown);
        processSubscription(
                () -> Stream.of(Map.entry(zone, zoneClientReferenceTimes.getOrDefault(zone, Map.of()))),
                ZoneDetectionSubscriptionType.topic(zone), SUBSCRIBE_ZONE_DETECTIONS_RESULT, listener, includeLastKnown);
    }

    private void processSubscription(Supplier<Stream<Map.Entry<ZonePK, Map<String, Instant>>>> detectionSupplier,
                                     Topic<List<ResolvedSiteDetection>> topic,
                                     String resultTypeName,
                                     MessageListener<List<ResolvedSiteDetection>> listener,
                                     boolean includeLastKnown) {
        if (includeLastKnown) {
            awaitLastKnownPreloaded();

            var cutoff = Instant.now().minus(LAST_KNOWN_DETECTIONS_CUTOFF);
            var lastKnownDetections = detectionSupplier.get()
                    .flatMap(e -> e.getValue().entrySet().stream()
                            .filter(e2 -> e2.getValue().isAfter(cutoff))
                            .map(e2 -> new ResolvedSiteDetection(e2.getValue(), e2.getKey(), e.getKey().zoneId)))
                    .collect(toUnmodifiableList());

            if (!lastKnownDetections.isEmpty())
                listener.accept(resultTypeName, lastKnownDetections);
        }

        subscriptionManager.addSubscription(topic,
                new SubscriptionHandler<>(listener.getSession(), listener.getCorrelationId(),
                        detections -> listener.accept(resultTypeName, detections)));
    }

    private void awaitLastKnownPreloaded() {
        try {
            // N.B.: This is practical only if preloading takes a few seconds, roughly up to a total of
            //       500,000 detections in the period between [now - cut-off] and [now].
            if (!lastKnownPreloaded.await(LAST_KNOWN_DETECTIONS_PRELOAD_TIMEOUT.toMillis(), MILLISECONDS))
                throw new RuntimeException("Timed out while waiting for preload");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateSnapshot(DetectionMatch match) {
        if (!match.clientReference.isPresent()) return;

        var clientReference = match.clientReference.get();
        var zoneId = match.zone.zoneId;
        var detectionTime = match.detection.detectionTime;
        var siteAndClientReference = new SiteAndClientReference(match.zone.getSite(), clientReference);
        var originalZoneId = clientReferenceZones.get(siteAndClientReference);

        // `clientReferenceZones` and `zoneClientReferenceTimes` are different views of reality and here we update them
        // and keep them in sync. The latter is useful for reading, the former acts as an index for updates.
        // There is no explicit synchronization between updates to `zoneClientReferenceTimes` and
        // `clientReferenceZones`, which may result in concurrency anomalies. The worst case is not recording
        // a "last known" detection due to a race only if they move very fast between zones or a GC event occurs but
        // even this will very likely get corrected when another detection arrives for the clientReference in question.
        if (zoneId.equals(originalZoneId)) { // Re-detected in the same zone
            zoneClientReferenceTimes
                    .computeIfAbsent(match.zone, z -> new ConcurrentHashMap<>())
                    .merge(clientReference, detectionTime, Ordering.natural()::max);
        } else {
            var isAfterLastKnown = true;
            if (originalZoneId != null) { // Detected in a new zone
                var originalZone = new ZonePK(match.zone.clientId, match.zone.siteId, originalZoneId);
                var clientReferenceDetectionTimePairsForZone =
                        zoneClientReferenceTimes.computeIfAbsent(originalZone, z -> new ConcurrentHashMap<>());
                var previousDetectionTime = clientReferenceDetectionTimePairsForZone.get(clientReference);
                if (detectionTime.isAfter(previousDetectionTime)) {
                    clientReferenceDetectionTimePairsForZone.remove(clientReference, previousDetectionTime);
                } else {
                    isAfterLastKnown = false;
                }
            }

            if (isAfterLastKnown) {
                zoneClientReferenceTimes
                        .computeIfAbsent(match.zone, z -> new ConcurrentHashMap<>())
                        .put(clientReference, detectionTime);
                clientReferenceZones.put(siteAndClientReference, zoneId);
            }
        }
    }

    private void loadLastKnown(DetectionMatcher detectionMatcher, Database db) {
        var timestampLimit = now().minus(LAST_KNOWN_DETECTIONS_CUTOFF);

        log.info("Preloading last known detections");
        var timerStart = System.currentTimeMillis();
        db.execute(sql -> {
            sql.select(
                    RFID_DETECTION.CLIENT_ID,
                    RFID_DETECTION.DETECTABLE_ID,
                    RFID_DETECTION.DETECTABLE_TYPE,
                    RFID_DETECTION.READER_SN,
                    RFID_DETECTION.PORT_NUMBER,
                    RFID_DETECTION.DETECTION_TIME)
                    .distinctOn(RFID_DETECTION.CLIENT_ID, RFID_DETECTION.DETECTABLE_ID, RFID_DETECTION.DETECTABLE_TYPE)
                    .from(RFID_DETECTION)
                    .join(CLIENT_CONFIG)
                    .using(CLIENT_CONFIG.CLIENT_ID)
                    .where(CLIENT_CONFIG.LIVE_VIEW_ENABLED) // A bit dirty but probably matches business reqs quite well
                    .and(RFID_DETECTION.DETECTION_TIME.gt(timestampLimit))
                    .orderBy(
                            RFID_DETECTION.CLIENT_ID,
                            RFID_DETECTION.DETECTABLE_ID,
                            RFID_DETECTION.DETECTABLE_TYPE,
                            RFID_DETECTION.DETECTION_TIME.desc())
                    .stream()
                    .forEach(r -> detectionMatcher.match(getDetectionFromRecord(r)).ifPresent(this::updateSnapshot));
            return null;
        });
        lastKnownPreloaded.countDown();
        log.info("Preloaded last known detections in {} ms", System.currentTimeMillis() - timerStart);
    }

    private static Detection getDetectionFromRecord(Record r) {
        return new Detection(
                new DetectablePK(
                        r.get(RFID_DETECTION.CLIENT_ID),
                        r.get(RFID_DETECTION.DETECTABLE_ID),
                        DetectableType.fromString(r.get(RFID_DETECTION.DETECTABLE_TYPE))),
                r.get(RFID_DETECTION.READER_SN),
                r.get(RFID_DETECTION.PORT_NUMBER),
                r.get(RFID_DETECTION.DETECTION_TIME),
                Optional.empty(), 1);
    }

    private static class SiteAndClientReference {
        final SitePK site;
        final String clientReference;

        SiteAndClientReference(SitePK site, String clientReference) {
            this.site = site;
            this.clientReference = clientReference;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            var that = (SiteAndClientReference) o;
            return Objects.equals(site, that.site) &&
                    Objects.equals(clientReference, that.clientReference);
        }

        public int hashCode() {
            return Objects.hash(site, clientReference);
        }
    }
}
