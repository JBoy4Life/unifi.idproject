package id.unifi.service.core;

import id.unifi.service.common.operator.OperatorPK;
import id.unifi.service.common.security.Token;

public class OperatorSessionData {
    private Token sessionToken;
    private OperatorPK operator;

    public OperatorSessionData() {}

    public synchronized OperatorPK getOperator() {
        return operator;
    }

    public synchronized Token getSessionToken() {
        return sessionToken;
    }

    public synchronized void setAuth(Token sessionToken, OperatorPK operator) {
        this.sessionToken = sessionToken;
        this.operator = operator;
    }
}
