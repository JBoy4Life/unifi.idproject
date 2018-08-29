package id.unifi.service.common.api.errors;

import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;

public class AlreadyExists extends CoreMarshallableError implements HttpMarshallableError {
    public final String type;

    public AlreadyExists(String type) {
        super("already-exists", type + " already exists");
        this.type = type;
    }

    public int getHttpStatusCode() {
        return SC_CONFLICT;
    }
}
