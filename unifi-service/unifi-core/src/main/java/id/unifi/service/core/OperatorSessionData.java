package id.unifi.service.core;

import id.unifi.service.common.operator.OperatorPK;

public class OperatorSessionData {
    private byte[] sessionToken;
    private OperatorPK operator;

    public OperatorSessionData() {}

    public synchronized OperatorPK getOperator() {
        return operator;
    }

    public synchronized byte[] getSessionToken() {
        return sessionToken;
    }

    public synchronized void setAuth(byte[] sessionToken, OperatorPK operator) {
        this.sessionToken = sessionToken;
        this.operator = operator;
    }
}
