package id.unifi.service.core.processing.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import id.unifi.service.common.api.MessageListener;
import id.unifi.service.common.detection.DetectionMatch;
import id.unifi.service.common.detection.DetectionMatchListener;
import id.unifi.service.common.subscriptions.SubscriptionHandler;
import id.unifi.service.common.subscriptions.SubscriptionManager;
import id.unifi.service.common.types.pk.DetectablePK;
import id.unifi.service.common.types.pk.SitePK;
import id.unifi.service.core.site.ResolvedSiteDetection;
import id.unifi.service.core.site.SiteDetectionSubscriptionType;
import static id.unifi.service.core.site.SiteDetectionSubscriptionType.topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DetectionSubscriber implements DetectionMatchListener {
    private static final Logger log = LoggerFactory.getLogger(DetectionSubscriber.class);

    private final Cache<DetectablePK, Boolean> recentDetectables;
    private final SubscriptionManager subscriptionManager;

    public DetectionSubscriber(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
        this.recentDetectables = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();
    }

    public void accept(List<DetectionMatch> matches) {
        for (var match : matches) {
            var detectable = match.detection.detectable;

            // FIXME: Limit by detection time, not wall clock
            if (recentDetectables.getIfPresent(detectable) != null) continue;
            recentDetectables.put(detectable, true);

            match.clientReference.ifPresent(clientReference -> {
                var resolved = new ResolvedSiteDetection(match.detection.detectionTime, clientReference, match.zone.zoneId);
                var site = new SitePK(match.zone.clientId, match.zone.siteId);
                subscriptionManager.distributeMessage(topic(site), List.of(resolved));
            });
        }
    }

    public void addListener(SitePK site, MessageListener<List<ResolvedSiteDetection>> listener) {
        log.debug("Adding listener for {}", site);
        var topic = SiteDetectionSubscriptionType.topic(site);
        subscriptionManager.addSubscription(topic,
                new SubscriptionHandler<>(listener.getSession(), listener.getCorrelationId(),
                        detections -> listener.accept("core.site.subscribe-detections-result", detections)));
    }
}
