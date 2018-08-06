package id.unifi.service.provider.rfid.config;

import com.impinj.octane.ReaderMode;
import com.impinj.octane.SearchMode;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

public class ReaderConfig {
    public final Optional<ReaderMode> readerMode;
    public final Optional<SearchMode> searchMode;
    public final OptionalInt session;
    public final OptionalInt tagPopulationEstimate;
    public final Optional<Boolean> enableFastId;
    public final Optional<List<Double>> txFrequencies;
    public final Optional<DetectableFilter> filter;
    public final Optional<Boolean> disconnectedOperation;
    public final Optional<Map<Integer, AntennaConfig>> ports;

    public static final ReaderConfig empty =
            new ReaderConfig(Optional.empty(), Optional.empty(), OptionalInt.empty(), OptionalInt.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public static ReaderConfig fromPortNumbers(List<Integer> portNumbers) {
        var ports = portNumbers.stream().collect(toUnmodifiableMap(identity(), n -> AntennaConfig.empty));
        return new ReaderConfig(Optional.empty(), Optional.empty(), OptionalInt.empty(), OptionalInt.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(ports));
    }

    public ReaderConfig(Optional<ReaderMode> readerMode,
                        Optional<SearchMode> searchMode,
                        OptionalInt session,
                        OptionalInt tagPopulationEstimate,
                        Optional<Boolean> enableFastId,
                        Optional<List<Double>> txFrequencies,
                        Optional<DetectableFilter> filter,
                        Optional<Boolean> disconnectedOperation,
                        Optional<Map<Integer, AntennaConfig>> ports) {
        this.readerMode = readerMode;
        this.searchMode = searchMode;
        this.session = session;
        this.tagPopulationEstimate = tagPopulationEstimate;
        this.enableFastId = enableFastId;
        this.txFrequencies = txFrequencies;
        this.filter = filter;
        this.disconnectedOperation = disconnectedOperation;
        this.ports = ports;
    }

    public ReaderConfig copyWithPorts(Optional<Map<Integer, AntennaConfig>> newPorts) {
        return new ReaderConfig(
                readerMode, searchMode, session, tagPopulationEstimate, enableFastId, txFrequencies, filter,
                disconnectedOperation, newPorts);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (ReaderConfig) o;
        return Objects.equals(readerMode, that.readerMode) &&
                Objects.equals(searchMode, that.searchMode) &&
                Objects.equals(session, that.session) &&
                Objects.equals(tagPopulationEstimate, that.tagPopulationEstimate) &&
                Objects.equals(enableFastId, that.enableFastId) &&
                Objects.equals(txFrequencies, that.txFrequencies) &&
                Objects.equals(filter, that.filter) &&
                Objects.equals(disconnectedOperation, that.disconnectedOperation) &&
                Objects.equals(ports, that.ports);
    }

    public int hashCode() {
        return Objects.hash(
                readerMode, searchMode, session, tagPopulationEstimate, enableFastId, txFrequencies, filter,
                disconnectedOperation, ports);
    }

    public String toString() {
        return "ReaderConfig{" +
                "readerMode=" + readerMode +
                ", searchMode=" + searchMode +
                ", session=" + session +
                ", tagPopulationEstimate=" + tagPopulationEstimate +
                ", enableFastId=" + enableFastId +
                ", txFrequencies=" + txFrequencies +
                ", filter=" + filter +
                ", disconnectedOperation=" + disconnectedOperation +
                ", ports=" + ports +
                '}';
    }
}
