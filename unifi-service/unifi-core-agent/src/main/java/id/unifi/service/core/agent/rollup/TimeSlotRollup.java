package id.unifi.service.core.agent.rollup;

import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.detection.RawDetection;
import id.unifi.service.common.detection.RawDetectionReport;
import static java.util.Collections.max;
import static java.util.Collections.min;
import static java.util.stream.Collectors.toList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Rolls up detections into fixed-sized time slots, taking the first detection time and highest RSSI.
 *
 * Not thread-safe.
 */
public class TimeSlotRollup implements Rollup {
    private static final Logger log = LoggerFactory.getLogger(TimeSlotRollup.class);

    private final int intervalSeconds;
    private final Map<AntennaId, AntennaState> antennaStates;

    TimeSlotRollup(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
        this.antennaStates = new HashMap<>();
    }

    public Stream<RawDetectionReport> process(RawDetectionReport report) {
        List<RawDetectionReport> reports = new ArrayList<>();
        for (RawDetection detection : report.detections) {
            Detectable detectable = new Detectable(detection.detectableId, detection.detectableType);
            AntennaId antennaId = new AntennaId(report.readerSn, detection.portNumber);

            Instant detectionSlotStart =
                    Instant.ofEpochSecond(detection.timestamp.getEpochSecond() / intervalSeconds * intervalSeconds, 0L);

            AntennaState currentAntennaState = antennaStates.computeIfAbsent(antennaId,
                    id -> new AntennaState(detectionSlotStart, new HashMap<>()));
            DetectableState state = currentAntennaState.detectableStates.computeIfAbsent(detectable,
                    id -> emptyState(detectionSlotStart));
            if (detection.timestamp.isBefore(currentAntennaState.slotStart)) {
                // Old detection that should've been processed
                log.debug("Ignoring old detection at {}, current slot for {} is {}",
                        detection.timestamp, antennaId, currentAntennaState.slotStart);
            } else if (detection.timestamp.isBefore(slotEnd(currentAntennaState))) {
                // Detection is in the current slot for antenna
                currentAntennaState.detectableStates.put(detectable, updateState(state, detection));
            } else {
                // Detection happened after the current time slot, we're rolling up
                AntennaState updatedState = new AntennaState(detectionSlotStart, new HashMap<>());
                updatedState.detectableStates.put(detectable, updateState(emptyState(detectionSlotStart), detection));
                antennaStates.put(antennaId, updatedState);

                List<RawDetection> pastDetections = currentAntennaState.detectableStates.entrySet().stream()
                        .map(e -> new RawDetection(
                                e.getValue().firstSeen,
                                antennaId.portNumber,
                                e.getKey().detectableId,
                                e.getKey().detectableType,
                                e.getValue().rssi,
                                e.getValue().count))
                        .collect(toList());

                reports.add(new RawDetectionReport(report.readerSn, pastDetections));
            }
        }
        if (!reports.isEmpty()) {
            for (RawDetectionReport r : reports) {
                log.debug("Rolling up for {}: {}", r.readerSn, r.detections);
            }
        }

        return reports.stream();
    }

    private DetectableState emptyState(Instant detectableSlotStart) {
        return new DetectableState(detectableSlotStart, BigDecimal.valueOf(Long.MIN_VALUE), 0);
    }

    private static DetectableState updateState(DetectableState state, RawDetection detection) {
        return new DetectableState(
                min(List.of(detection.timestamp, state.firstSeen)),
                max(List.of(detection.rssi, state.rssi)),
                state.count + detection.count
        );
    }

    private Instant slotEnd(AntennaState antennaState) {
        return antennaState.slotStart.plusSeconds(intervalSeconds);
    }

    private static class AntennaState {
        final Instant slotStart;
        final Map<Detectable, DetectableState> detectableStates;

        AntennaState(Instant slotStart, Map<Detectable, DetectableState> detectableStates) {
            this.slotStart = slotStart;
            this.detectableStates = detectableStates;
        }
    }

    private static class DetectableState {
        final Instant firstSeen;
        final BigDecimal rssi;
        final int count;

        DetectableState(Instant firstSeen, BigDecimal rssi, int count) {
            this.firstSeen = firstSeen;
            this.rssi = rssi;
            this.count = count;
        }
    }

    private static class AntennaId {
        final String readerSn;
        final int portNumber;

        AntennaId(String readerSn, int portNumber) {
            this.readerSn = readerSn;
            this.portNumber = portNumber;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AntennaId antennaId = (AntennaId) o;
            return portNumber == antennaId.portNumber &&
                    Objects.equals(readerSn, antennaId.readerSn);
        }

        public int hashCode() {
            return Objects.hash(readerSn, portNumber);
        }

        public String toString() {
            return "AntennaId{" +
                    "readerSn='" + readerSn + '\'' +
                    ", portNumber=" + portNumber +
                    '}';
        }
    }

    private static class Detectable {
        final String detectableId;
        final DetectableType detectableType;

        Detectable(String detectableId, DetectableType detectableType) {
            this.detectableId = detectableId;
            this.detectableType = detectableType;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Detectable that = (Detectable) o;
            return Objects.equals(detectableId, that.detectableId) &&
                    detectableType == that.detectableType;
        }

        public int hashCode() {
            return Objects.hash(detectableId, detectableType);
        }
    }
}
