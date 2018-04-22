package id.unifi.service.core.agent.rollup;

import id.unifi.service.core.agent.config.RollupConfig;

import java.util.Optional;

public class RollupUtils {
    private RollupUtils() {}

    public static Rollup rollupFromConfig(Optional<RollupConfig> config) {
        RollupConfig c = config.orElse(RollupConfig.empty);
        switch (c.strategy) {
            case NONE:
                return NullRollup.instance;
        }
        return null;
    }
}
