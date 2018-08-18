package id.unifi.service.common.api.errors;

import static javax.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;

public class UnsupportedMediaType extends CoreMarshallableError implements HttpMarshallableError {
    public UnsupportedMediaType(String mediaType) {
        super("unsupported-media-type", "Unsupported media type: " + mediaType);
    }

    public int getHttpStatusCode() {
        return SC_UNSUPPORTED_MEDIA_TYPE;
    }
}
