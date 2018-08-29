package id.unifi.service.core.processing.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Ordering;
import id.unifi.service.common.api.MessageListener;
import id.unifi.service.common.detection.DetectionMatch;
import id.unifi.service.common.detection.DetectionMatchListener;
import id.unifi.service.common.subscriptions.SubscriptionHandler;
import id.unifi.service.common.subscriptions.SubscriptionManager;
import id.unifi.service.common.types.pk.DetectablePK;
import id.unifi.service.common.types.pk.SitePK;
import id.unifi.service.common.types.pk.ZonePK;
import id.unifi.service.core.site.ResolvedSiteDetection;
import id.unifi.service.core.site.SiteDetectionSubscriptionType;
import id.unifi.service.core.site.ZoneDetectionSubscriptionType;
import static java.util.stream.Collectors.toUnmodifiableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DetectionSubscriber implements DetectionMatchListener {
    private static final Logger log = LoggerFactory.getLogger(DetectionSubscriber.class);

    private static final String SUBSCRIBE_DETECTIONS_RESULT = "core.site.subscribe-detections-result";
    private static final String SUBSCRIBE_ZONE_DETECTIONS_RESULT = "core.site.subscribe-zone-detections-result";

    private final Cache<DetectablePK, Boolean> recentDetectables;
    private final SubscriptionManager subscriptionManager;
    private final Map<SiteAndClientReference, String> clientReferenceZones;
    private final Map<ZonePK, Map<String, Instant>> zoneClientReferenceTimes;

    public DetectionSubscriber(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
        this.recentDetectables = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();
        this.clientReferenceZones = new ConcurrentHashMap<>();
        this.zoneClientReferenceTimes = new ConcurrentHashMap<>();
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
        var topic = SiteDetectionSubscriptionType.topic(site);

        if (includeLastKnown) {
            var lastKnownDetections = zoneClientReferenceTimes.entrySet().stream()
                    .filter(e -> e.getKey().getSite().equals(site))
                    .flatMap(e -> e.getValue().entrySet().stream()
                            .map(e2 -> new ResolvedSiteDetection(e2.getValue(), e2.getKey(), e.getKey().zoneId)))
                    .collect(toUnmodifiableList());

            if (!lastKnownDetections.isEmpty())
                listener.accept(SUBSCRIBE_DETECTIONS_RESULT, lastKnownDetections);
        }

        subscriptionManager.addSubscription(topic,
                new SubscriptionHandler<>(listener.getSession(), listener.getCorrelationId(),
                        detections -> listener.accept(SUBSCRIBE_DETECTIONS_RESULT, detections)));
    }

    public void addListener(ZonePK zone,
                            MessageListener<List<ResolvedSiteDetection>> listener,
                            boolean includeLastKnown) {
        log.debug("Adding listener for {}, includeLastKnown: {}", zone, includeLastKnown);
        var topic = ZoneDetectionSubscriptionType.topic(zone);

        if (includeLastKnown) {
            var lastKnownDetections = zoneClientReferenceTimes.getOrDefault(zone, Map.of()).entrySet().stream()
                    .map(e -> new ResolvedSiteDetection(e.getValue(), e.getKey(), zone.zoneId))
                    .collect(toUnmodifiableList());

            if (!lastKnownDetections.isEmpty())
                listener.accept(SUBSCRIBE_ZONE_DETECTIONS_RESULT, lastKnownDetections);
        }

        subscriptionManager.addSubscription(topic,
                new SubscriptionHandler<>(listener.getSession(), listener.getCorrelationId(),
                        detections -> listener.accept(SUBSCRIBE_ZONE_DETECTIONS_RESULT, detections)));
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
