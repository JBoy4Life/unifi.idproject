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
    private final Map<String, ReaderState> readerStates;

    TimeSlotRollup(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
        this.readerStates = new HashMap<>();
    }

    public Stream<RawDetectionReport> process(RawDetectionReport report) {
        List<RawDetectionReport> reports = new ArrayList<>();
        var readerSn = report.readerSn;
        for (var detection : report.detections) {
            var antennaDetectable =
                    new AntennaDetectable(detection.portNumber, detection.detectableId, detection.detectableType);
            var detectionSlotStart =
                    Instant.ofEpochSecond(detection.timestamp.getEpochSecond() / intervalSeconds * intervalSeconds, 0L);

            var currentReaderState = readerStates.computeIfAbsent(readerSn,
                    sn -> new ReaderState(detectionSlotStart, new HashMap<>()));
            var state = currentReaderState.states.computeIfAbsent(antennaDetectable,
                    ad -> emptyState(detectionSlotStart));

            if (detection.timestamp.isBefore(currentReaderState.slotStart)) {
                // Old detection that should've been processed
                log.debug("Ignoring old detection at {}, current slot for {}/{} is {}",
                        detection.timestamp, readerSn, detection.portNumber, currentReaderState.slotStart);
            } else if (detection.timestamp.isBefore(slotEnd(currentReaderState))) {
                // Detection is in the current slot for antenna
                currentReaderState.states.put(antennaDetectable, updateState(state, detection));
            } else {
                // Detection happened after the current time slot, we're rolling up
                var updatedState = new ReaderState(detectionSlotStart, new HashMap<>());
                updatedState.states.put(antennaDetectable, updateState(emptyState(detectionSlotStart), detection));
                readerStates.put(readerSn, updatedState);

                var pastDetections = currentReaderState.states.entrySet().stream()
                        .map(e -> new RawDetection(
                                e.getValue().firstSeen,
                                e.getKey().portNumber,
                                e.getKey().detectableId,
                                e.getKey().detectableType,
                                e.getValue().rssi,
                                e.getValue().count))
                        .collect(toList());

                reports.add(new RawDetectionReport(readerSn, pastDetections));
            }
        }

        return reports.stream();
    }

    private AntennaDetectableState emptyState(Instant detectableSlotStart) {
        var firstSeen = detectableSlotStart.plusSeconds(intervalSeconds);
        return new AntennaDetectableState(firstSeen, BigDecimal.valueOf(Long.MIN_VALUE), 0);
    }

    private static AntennaDetectableState updateState(AntennaDetectableState state, RawDetection detection) {
        return new AntennaDetectableState(
                min(List.of(detection.timestamp, state.firstSeen)),
                max(List.of(detection.rssi, state.rssi)),
                state.count + detection.count
        );
    }

    private Instant slotEnd(ReaderState readerState) {
        return readerState.slotStart.plusSeconds(intervalSeconds);
    }

    private static class ReaderState {
        final Instant slotStart;
        final Map<AntennaDetectable, AntennaDetectableState> states;

        ReaderState(Instant slotStart, Map<AntennaDetectable, AntennaDetectableState> states) {
            this.slotStart = slotStart;
            this.states = states;
        }
    }

    private static class AntennaDetectableState {
        final Instant firstSeen;
        final BigDecimal rssi;
        final int count;

        AntennaDetectableState(Instant firstSeen, BigDecimal rssi, int count) {
            this.firstSeen = firstSeen;
            this.rssi = rssi;
            this.count = count;
        }
    }

    private static class AntennaDetectable {
        final int portNumber;
        final String detectableId;
        final DetectableType detectableType;

        AntennaDetectable(int portNumber, String detectableId, DetectableType detectableType) {
            this.portNumber = portNumber;
            this.detectableId = detectableId;
            this.detectableType = detectableType;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            var that = (AntennaDetectable) o;
            return portNumber == that.portNumber &&
                    Objects.equals(detectableId, that.detectableId) &&
                    detectableType == that.detectableType;
        }

        public int hashCode() {
            return Objects.hash(portNumber, detectableId, detectableType);
        }
    }
}
