package id.unifi.service.common.api;

import com.google.common.base.CaseFormat;
import com.google.common.net.MediaType;

public enum Protocol {
    JSON(false, MediaType.JSON_UTF_8), MSGPACK(true, MediaType.create("application", "x-msgpack"));

    private final boolean binary;
    private final MediaType mediaType;
    private final String stringName;

    Protocol(boolean binary, MediaType mediaType) {
        this.binary = binary;
        this.mediaType = mediaType;
        this.stringName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, name());
    }

    public boolean isBinary() {
        return binary;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String toString() {
        return stringName;
    }
}
