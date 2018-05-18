package id.unifi.service.core.processing;

import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.detection.Detection;
import id.unifi.service.common.detection.DetectionMatch;
import id.unifi.service.common.types.pk.AntennaPK;
import id.unifi.service.common.types.pk.DetectablePK;
import id.unifi.service.common.types.pk.ZonePK;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.ANTENNA;
import static id.unifi.service.core.db.Tables.ASSIGNMENT;
import static id.unifi.service.core.db.Tables.DETECTABLE;
import static java.util.stream.Collectors.toUnmodifiableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DetectionMatcher {
    private static final Logger log = LoggerFactory.getLogger(DetectionMatcher.class);
    private static final Duration ASSIGNMENT_REFRESH_RATE = Duration.ofMinutes(1);

    private final Database db;
    private final ScheduledExecutorService refreshScheduler;

    private volatile Assignments assignments;
    private long lastRefreshMillis;

    public DetectionMatcher(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
        this.refreshAssignments();
        refreshScheduler = Executors.newSingleThreadScheduledExecutor();
        refreshScheduler.scheduleAtFixedRate(this::refreshAssignments,
                ASSIGNMENT_REFRESH_RATE.toMillis(), ASSIGNMENT_REFRESH_RATE.toMillis(), TimeUnit.MILLISECONDS);
    }

    public Optional<DetectionMatch> match(Detection detection) {
        var clientId = detection.detectable.clientId;

        var clientReference = assignments.detectableHolders.get(detection.detectable);
        if (clientReference == null) {
            // Entry for detectable is missing, we're dealing with an unknown detectable.
            log.trace("Skipping unknown detectable: {}", detection.detectable);
            return Optional.empty();
        }

        var antenna = new AntennaPK(clientId, detection.readerSn, detection.portNumber);
        var zone = assignments.antennaZones.get(antenna);
        if (zone == null) {
            log.trace("Skipping unassigned {}", antenna);
            return Optional.empty();
        }

        return Optional.of(new DetectionMatch(detection, zone, clientReference));
    }

    private void refreshAssignments() {
        var timerStart = System.currentTimeMillis();

        this.assignments = db.execute(sql -> {
            var detectableHolders = sql
                    .select(DETECTABLE.CLIENT_ID,
                            DETECTABLE.DETECTABLE_ID,
                            DETECTABLE.DETECTABLE_TYPE,
                            ASSIGNMENT.CLIENT_REFERENCE)
                    .from(DETECTABLE.leftJoin(ASSIGNMENT).onKey())
                    .where(DETECTABLE.ACTIVE)
                    .stream()
                    .collect(toUnmodifiableMap(
                            d -> new DetectablePK(d.value1(), d.value2(), DetectableType.fromString(d.value3())),
                            r -> Optional.ofNullable(r.value4())));

            var antennaZones = sql
                    .selectFrom(ANTENNA)
                    .stream()
                    .collect(toUnmodifiableMap(
                            a -> new AntennaPK(a.getClientId(), a.getReaderSn(), a.getPortNumber()),
                            a -> new ZonePK(a.getClientId(), a.getSiteId(), a.getZoneId())
                    ));

            lastRefreshMillis = System.currentTimeMillis();
            log.info("Refreshed core assignments in {} ms: {} detectables, {} antennae",
                    lastRefreshMillis - timerStart, detectableHolders.size(), antennaZones.size());
            return new Assignments(detectableHolders, antennaZones);
        });
    }

    private static class Assignments {
        final Map<DetectablePK, Optional<String>> detectableHolders;
        final Map<AntennaPK, ZonePK> antennaZones;

        Assignments(Map<DetectablePK, Optional<String>> detectableHolders, Map<AntennaPK, ZonePK> antennaZones) {
            this.detectableHolders = detectableHolders;
            this.antennaZones = antennaZones;
        }
    }
}
