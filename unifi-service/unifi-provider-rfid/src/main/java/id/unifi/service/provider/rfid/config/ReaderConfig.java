package id.unifi.service.provider.rfid.config;

import com.impinj.octane.ReaderMode;
import com.impinj.octane.SearchMode;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public class ReaderConfig {
    public final Optional<ReaderMode> readerMode;
    public final Optional<SearchMode> searchMode;
    public final OptionalInt session;
    public final OptionalInt tagPopulationEstimate;
    public final Optional<List<Double>> txFrequencies;
    public final Optional<List<UhfDetectableType>> detectableTypes;
    public final Optional<DetectableFilter> filter;
    public final Optional<Map<Integer, AntennaConfig>> ports;

    public static final ReaderConfig empty =
            new ReaderConfig(Optional.empty(), Optional.empty(), OptionalInt.empty(), OptionalInt.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public ReaderConfig(Optional<ReaderMode> readerMode,
                        Optional<SearchMode> searchMode,
                        OptionalInt session,
                        OptionalInt tagPopulationEstimate,
                        Optional<List<Double>> txFrequencies,
                        Optional<List<UhfDetectableType>> detectableTypes,
                        Optional<DetectableFilter> filter,
                        Optional<Map<Integer, AntennaConfig>> ports) {
        this.readerMode = readerMode;
        this.searchMode = searchMode;
        this.session = session;
        this.tagPopulationEstimate = tagPopulationEstimate;
        this.txFrequencies = txFrequencies;
        this.detectableTypes = detectableTypes;
        this.filter = filter;
        this.ports = ports;
    }

    public static ReaderConfig fromPortNumbers(List<Integer> portNumbers) {
        Map<Integer, AntennaConfig> antennae =
                portNumbers.stream().collect(toMap(identity(), n -> AntennaConfig.empty));
        return new ReaderConfig(Optional.empty(), Optional.empty(), OptionalInt.empty(), OptionalInt.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(antennae));
    }
}
