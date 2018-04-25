package id.unifi.service.core.processing.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import id.unifi.service.common.api.MessageListener;
import id.unifi.service.common.detection.DetectionMatch;
import id.unifi.service.common.detection.DetectionMatchListener;
import id.unifi.service.common.types.pk.DetectablePK;
import id.unifi.service.common.types.pk.SitePK;
import id.unifi.service.core.site.ResolvedDetection;
import static java.util.Collections.newSetFromMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DetectionSubscriber implements DetectionMatchListener {
    private static final Logger log = LoggerFactory.getLogger(DetectionSubscriber.class);

    private static final String DETECTIONS_MESSAGE_TYPE = "core.site.detection-report";

    private final LoadingCache<SitePK, Set<MessageListener<List<ResolvedDetection>>>> detectionListeners;
    private final Cache<DetectablePK, Boolean> recentDetectables;

    public DetectionSubscriber() {
        this.detectionListeners = CacheBuilder.newBuilder()
                .build(CacheLoader.from(s -> newSetFromMap(new HashMap<>())));
        this.recentDetectables = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();
    }

    public void accept(List<DetectionMatch> matches) {
        for (var match : matches) {
            var detectable = match.detection.detectable;

            // FIXME: Limit by detection time, not wall clock
            if (recentDetectables.getIfPresent(detectable) != null) return;
            recentDetectables.put(detectable, true);

            var listeners = detectionListeners.getIfPresent(new SitePK(match.zone.clientId, match.zone.siteId));
            if (listeners == null || listeners.isEmpty()) return;

            if (match.clientReference.isPresent()) {
                var resolvedDetection = new ResolvedDetection(
                        match.detection.detectionTime,
                        match.clientReference.get(),
                        match.zone.zoneId);
                listeners.forEach(l -> l.accept(DETECTIONS_MESSAGE_TYPE, List.of(resolvedDetection)));
            }
        }
    }

    public void addListener(SitePK sitePK, MessageListener<List<ResolvedDetection>> listener) {
        detectionListeners.getUnchecked(sitePK).add(listener);
    }
}
