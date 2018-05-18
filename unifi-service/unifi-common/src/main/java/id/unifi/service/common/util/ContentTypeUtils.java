package id.unifi.service.common.util;

import id.unifi.service.common.api.errors.ValidationFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;

public class ContentTypeUtils {
    private static final Logger log = LoggerFactory.getLogger(ContentTypeUtils.class);

    public static Optional<ImageWithType> imageWithType(byte[] data) {
        String mimeType;
        try {
            mimeType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(data));
        } catch (IOException ignored) {
            return Optional.empty();
        }

        if ("application/xml".equals(mimeType)) mimeType = "image/svg+xml";

        if (mimeType == null || !mimeType.startsWith("image/")) {
            log.warn("Ignoring image of unrecognizable type: {}", mimeType);
            return Optional.empty();
        }

        return Optional.of(new ImageWithType(data, mimeType));
    }

    public static Optional<ImageWithType> validateImageFormat(Optional<byte[]> image) {
        return image.map(im -> ContentTypeUtils.imageWithType(im).orElseThrow(() -> new ValidationFailure(
                List.of(new ValidationFailure.ValidationError("image", ValidationFailure.Issue.BAD_FORMAT)))));
    }

    public static class ImageWithType {
        public final String mimeType;
        public final byte[] data;

        public ImageWithType(byte[] data, String mimeType) {
            this.mimeType = mimeType;
            this.data = data;
        }
    }
}
