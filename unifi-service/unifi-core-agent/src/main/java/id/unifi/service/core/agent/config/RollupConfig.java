package id.unifi.service.core.agent.config;

import id.unifi.service.core.agent.rollup.RollupStrategy;

import java.util.OptionalInt;

public class RollupConfig {
    public static final RollupConfig empty = new RollupConfig(RollupStrategy.NONE, OptionalInt.empty());

    public final RollupStrategy strategy;
    public final OptionalInt intervalSeconds;

    public RollupConfig(RollupStrategy strategy, OptionalInt intervalSeconds) {
        if (intervalSeconds.isPresent() == (strategy == RollupStrategy.NONE))
            throw new IllegalArgumentException("'intervalSeconds' is required iff 'strategy' is defined.");

        intervalSeconds.ifPresent(interval -> {
            if (interval <= 0 || 86400 % interval != 0)
                throw new IllegalArgumentException("Roll-up interval must be positive and divide 86400");
        });

        this.strategy = strategy;
        this.intervalSeconds = intervalSeconds;
    }
}
