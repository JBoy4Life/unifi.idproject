package id.unifi.service.core.agent.rollup;

import id.unifi.service.common.detection.RawDetectionReport;

import java.util.stream.Stream;

public class NullRollup implements Rollup {
    static final NullRollup instance = new NullRollup();

    private NullRollup() {}

    public Stream<RawDetectionReport> process(RawDetectionReport report) {
        return Stream.of(report);
    }
}
