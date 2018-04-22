package id.unifi.service.common.agent;

import java.util.List;
import java.util.Optional;

/**
 * Agent config data object that hides provider-specific properties behind a JSON object to avoid core dependency
 * on providers.
 */
public class AgentFullConfig<A, R> {
    public final Optional<A> agent;
    //public final Optional<List<DetectableType>> detectableTypes;
    public final List<ReaderFullConfig<R>> readers;

    public AgentFullConfig(Optional<A> agent, List<ReaderFullConfig<R>> readers) {
        this.agent = agent;
        this.readers = readers;
    }
}
