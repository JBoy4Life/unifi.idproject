package id.unifi.service.common.operator;

import com.google.common.cache.CacheBuilder;
import id.unifi.service.common.security.Token;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class InMemorySessionTokenStore implements SessionTokenStore {
    private final ConcurrentMap<Token, OperatorPK> store;

    public InMemorySessionTokenStore(long expirySeconds) {
        this.store = CacheBuilder.newBuilder()
                .expireAfterAccess(expirySeconds, TimeUnit.SECONDS)
                .<Token, OperatorPK>build()
                .asMap();
    }

    public Optional<OperatorPK> get(Token token) {
        return Optional.ofNullable(store.get(token));
    }

    public void put(Token token, OperatorPK operator) {
        store.put(token, operator);
    }

    public void remove(Token token) {
        store.remove(token);
    }
}
