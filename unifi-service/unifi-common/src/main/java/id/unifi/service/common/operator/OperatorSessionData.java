package id.unifi.service.common.operator;

import id.unifi.service.common.security.Token;
import id.unifi.service.common.types.pk.OperatorPK;

public class OperatorSessionData {
    private volatile AuthData auth;

    public OperatorSessionData() {}

    public OperatorSessionData(OperatorPK operatorPK, Token sessionToken) {
        setAuth(operatorPK, sessionToken);
    }

    public OperatorPK getOperator() {
        var auth = this.auth;
        return auth != null ? auth.operator : null;
    }

    public Token getSessionToken() {
        var auth = this.auth;
        return auth != null ? auth.sessionToken : null;
    }

    public void setAuth(OperatorPK operator, Token sessionToken) {
        this.auth = new AuthData(operator, sessionToken);
    }

    private static class AuthData {
        final OperatorPK operator;
        final Token sessionToken;

        AuthData(OperatorPK operator, Token sessionToken) {
            this.operator = operator;
            this.sessionToken = sessionToken;
        }
    }
}
