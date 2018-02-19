package id.unifi.service.common.operator;

import id.unifi.service.common.security.Token;
import id.unifi.service.common.types.OperatorPK;

import java.util.Optional;

public interface SessionTokenStore {
    Optional<OperatorPK> get(Token token);
    void put(Token token, OperatorPK operator);
    void remove(Token token);
}
