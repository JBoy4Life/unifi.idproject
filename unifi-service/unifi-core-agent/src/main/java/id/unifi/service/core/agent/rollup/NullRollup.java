package id.unifi.service.core.agent.rollup;

import id.unifi.service.common.detection.RawDetectionReport;

import java.util.List;

public class NullRollup implements Rollup {
    public static final NullRollup instance = new NullRollup();

    private NullRollup() {}

    public List<RawDetectionReport> process(RawDetectionReport report) {
        return List.of(report);
    }
}
