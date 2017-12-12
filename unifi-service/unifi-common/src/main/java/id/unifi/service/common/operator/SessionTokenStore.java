package id.unifi.service.common.operator;

import java.util.Optional;

public interface SessionTokenStore {
    Optional<OperatorPK> get(byte[] token);
    void put(byte[] token, OperatorPK operator);
    void remove(byte[] token);
}
