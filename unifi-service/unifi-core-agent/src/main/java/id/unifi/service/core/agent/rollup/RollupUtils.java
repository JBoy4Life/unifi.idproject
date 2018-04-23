package id.unifi.service.core.agent.rollup;

import id.unifi.service.core.agent.config.RollupConfig;

import java.util.Optional;

public class RollupUtils {
    private RollupUtils() {}

    public static Rollup rollupFromConfig(Optional<RollupConfig> config) {
        var c = config.orElse(RollupConfig.empty);
        switch (c.strategy) {
            case NONE:
                return NullRollup.instance;
            case TIME_SLOT:
                return new TimeSlotRollup(c.intervalSeconds.getAsInt());
            default:
                throw new IllegalArgumentException("Unexpected roll-up strategy: " + c.strategy);
        }
    }
}
