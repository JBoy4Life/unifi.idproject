package id.unifi.service.common.operator;

import com.google.common.cache.CacheBuilder;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class InMemorySessionTokenStore implements SessionTokenStore {
    private final ConcurrentMap<Bytes, OperatorPK> store;

    public InMemorySessionTokenStore(long expirySeconds) {
        this.store = CacheBuilder.newBuilder()
                .expireAfterAccess(expirySeconds, TimeUnit.SECONDS)
                .<Bytes, OperatorPK>build()
                .asMap();
    }

    private class Bytes {
        private final byte[] underlying;

        Bytes(byte[] underlying) {
            this.underlying = underlying;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Bytes bytes = (Bytes) o;
            return Arrays.equals(underlying, bytes.underlying);
        }

        public int hashCode() {
            return Arrays.hashCode(underlying);
        }
    }

    public Optional<OperatorPK> get(byte[] token) {
        return Optional.ofNullable(store.get(new Bytes(token)));
    }

    public void put(byte[] token, OperatorPK operator) {
        store.put(new Bytes(token), operator);
    }

    public void remove(byte[] token) {
        store.remove(new Bytes(token));
    }
}
