package id.unifi.service.common.api.errors;

import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;

public class NotAcceptable extends CoreMarshallableError implements HttpMarshallableError {
    public NotAcceptable() {
        super("not-acceptable", "Not acceptable");
    }

    public int getHttpStatusCode() {
        return SC_NOT_ACCEPTABLE;
    }
}
