package id.unifi.service.core.agent.config;

import id.unifi.service.core.agent.rollup.RollupStrategy;

import java.util.OptionalLong;

public class RollupConfig {
    public static final RollupConfig empty = new RollupConfig(RollupStrategy.NONE, OptionalLong.empty());

    public final RollupStrategy strategy;
    public final OptionalLong intervalSeconds;

    public RollupConfig(RollupStrategy strategy, OptionalLong intervalSeconds) {
        if (intervalSeconds.isPresent() == (strategy == RollupStrategy.NONE))
            throw new IllegalArgumentException("'intervalSeconds' is required iff 'strategy' is defined.");

        this.strategy = strategy;
        this.intervalSeconds = intervalSeconds;
    }
}
