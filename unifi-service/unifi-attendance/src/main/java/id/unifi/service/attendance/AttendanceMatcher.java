package id.unifi.service.attendance;

import static id.unifi.service.attendance.db.Attendance.ATTENDANCE;
import static id.unifi.service.attendance.db.Keys.BLOCK_ZONE__FK_BLOCK_ZONE_TO_BLOCK;
import id.unifi.service.attendance.db.Tables;
import static id.unifi.service.attendance.db.Tables.BLOCK;
import static id.unifi.service.attendance.db.Tables.BLOCK_TIME;
import static id.unifi.service.attendance.db.Tables.BLOCK_ZONE;
import static id.unifi.service.attendance.db.Tables.PROCESSING_STATE;
import id.unifi.service.attendance.types.pk.AssignmentPK;
import id.unifi.service.attendance.types.pk.AttendancePK;
import id.unifi.service.common.detection.DetectionMatch;
import id.unifi.service.common.types.pk.ZonePK;
import static id.unifi.service.common.util.TimeUtils.instantFromUtcLocal;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.ANTENNA;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

public class AttendanceMatcher {
    private static final Logger log = LoggerFactory.getLogger(AttendanceMatcher.class);

    public static final Duration DETECTION_BEFORE_BLOCK_START = Duration.ofMinutes(15);
    public static final Duration DETECTION_AFTER_BLOCK_END = Duration.ofMinutes(15);
    private static final Duration ASSIGNMENT_REFRESH_RATE = Duration.ofMinutes(1);

    private final Database db;
    private final ScheduledExecutorService refreshScheduler;

    private volatile Assignments assignments;
    private long lastRefreshMillis;

    public AttendanceMatcher(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE, ATTENDANCE);
        refreshAssignments();
        refreshScheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        refreshScheduler.scheduleAtFixedRate(
                this::refreshAssignments,
                ASSIGNMENT_REFRESH_RATE.toMillis(),
                ASSIGNMENT_REFRESH_RATE.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    public Stream<AttendancePK> match(DetectionMatch match) {
        if (!match.clientReference.isPresent()) return Stream.empty();
        var clientReference = match.clientReference.orElseThrow();

        var blocks = assignments.zoneBlocks.get(match.zone);
        if (blocks == null) {
            log.trace("No blocks in {}", match.zone);
            return Stream.empty();
        }

        var detection = match.detection;
        var detectionStartTime = detection.detectionTime.minus(blocks.maxBlockDuration);
        var subMap = blocks.blocks.subMap(detectionStartTime, true, detection.detectionTime, true);
        return subMap.values().stream()
                .filter(b -> assignments.contactSchedules.contains(new AssignmentPK(match.zone.clientId, clientReference, b.scheduleId)) &&
                        !detection.detectionTime.isBefore(b.detectionStartTime) &&
                        detection.detectionTime.isBefore(b.detectionEndTime))
                .map(b -> new AttendancePK(match.zone.clientId, clientReference, b.scheduleId, b.blockId));
    }

    private void refreshAssignments() {
        this.assignments = db.execute(sql -> {
            var timerStart = System.currentTimeMillis();

            var neverProcessedAntennae = sql
                    .selectFrom(ANTENNA.leftAntiJoin(PROCESSING_STATE)
                            .using(ANTENNA.CLIENT_ID, ANTENNA.READER_SN, ANTENNA.PORT_NUMBER))
                    .fetch();
            for (var antenna : neverProcessedAntennae) {
                sql.insertInto(PROCESSING_STATE)
                        .set(PROCESSING_STATE.CLIENT_ID, antenna.get(ANTENNA.CLIENT_ID))
                        .set(PROCESSING_STATE.READER_SN, antenna.get(ANTENNA.READER_SN))
                        .set(PROCESSING_STATE.PORT_NUMBER, antenna.get(ANTENNA.PORT_NUMBER))
                        .set(PROCESSING_STATE.PROCESSED_UP_TO, Instant.EPOCH.atOffset(ZoneOffset.UTC))
                        .onConflictDoNothing()
                        .execute();
                log.info("Added initial processing state for\n{}", antenna);
            }

            var contactSchedules = sql
                    .selectFrom(Tables.ASSIGNMENT)
                    .stream()
                    .map(r -> new AssignmentPK(r.getClientId(), r.getClientReference(), r.getScheduleId()))
                    .collect(toSet());

            var rawBlocksByZone = sql
                    .select(BLOCK.CLIENT_ID, BLOCK.SCHEDULE_ID, BLOCK.BLOCK_ID,
                            BLOCK_ZONE.SITE_ID, BLOCK_ZONE.ZONE_ID,
                            BLOCK_TIME.START_TIME,
                            BLOCK_TIME.END_TIME)
                    .from(BLOCK.join(BLOCK_TIME).onKey().join(BLOCK_ZONE).onKey(BLOCK_ZONE__FK_BLOCK_ZONE_TO_BLOCK))
                    .stream()
                    .collect(groupingBy(r -> new ZonePK(r.value1(), r.value4(), r.value5()),
                            mapping(r -> new Block(instantFromUtcLocal(r.value6()).minus(DETECTION_BEFORE_BLOCK_START),
                                            instantFromUtcLocal(r.value7()).plus(DETECTION_AFTER_BLOCK_END),
                                            r.value2(),
                                            r.value3()),
                                    toMap(b -> b.detectionStartTime, Function.identity(), (a, b) -> a, TreeMap::new))));
            var zoneBlocks = rawBlocksByZone.entrySet().stream()
                    .collect(toUnmodifiableMap(Map.Entry::getKey, e -> new ZoneBlocks(e.getValue())));

            lastRefreshMillis = System.currentTimeMillis();
            log.info("Refreshed attendance assignments in {} ms: {} schedule assignments, {} zones",
                    lastRefreshMillis - timerStart, contactSchedules.size(), zoneBlocks.size());
            return new Assignments(contactSchedules, zoneBlocks);
        });
    }

    private static class Assignments {
        final Set<AssignmentPK> contactSchedules;
        final Map<ZonePK, ZoneBlocks> zoneBlocks;

        Assignments(Set<AssignmentPK> contactSchedules, Map<ZonePK, ZoneBlocks> zoneBlocks) {
            this.contactSchedules = contactSchedules;
            this.zoneBlocks = zoneBlocks;
        }
    }

    private static class ZoneBlocks {
        final NavigableMap<Instant, Block> blocks;
        final Duration maxBlockDuration;

        private ZoneBlocks(NavigableMap<Instant, Block> blocks) {
            this.blocks = blocks;
            this.maxBlockDuration = blocks.values().stream()
                    .map(b -> Duration.between(b.detectionStartTime, b.detectionEndTime))
                    .max(naturalOrder())
                    .orElse(Duration.ZERO);
        }
    }

    private static class Block {
        final Instant detectionStartTime;
        final Instant detectionEndTime;
        final String scheduleId;
        final String blockId;

        Block(Instant detectionStartTime, Instant detectionEndTime, String scheduleId, String blockId) {
            this.detectionStartTime = detectionStartTime;
            this.detectionEndTime = detectionEndTime;
            this.scheduleId = scheduleId;
            this.blockId = blockId;
        }

        public String toString() {
            return "Block{" +
                    "detectionStartTime=" + detectionStartTime +
                    ", detectionEndTime=" + detectionEndTime +
                    ", scheduleId='" + scheduleId + '\'' +
                    ", blockId='" + blockId + '\'' +
                    '}';
        }
    }
}
