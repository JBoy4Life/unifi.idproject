package id.unifi.service.common.api;

import java.io.InputStream;
import java.io.Reader;
import java.util.Objects;

class MessageStream {
    final Reader reader;
    final InputStream inputStream;

    MessageStream(Reader reader) {
        this.reader = Objects.requireNonNull(reader);
        this.inputStream = null;
    }

    MessageStream(InputStream inputStream) {
        this.reader = null;
        this.inputStream = Objects.requireNonNull(inputStream);
    }

    public boolean isBinary() {
        return inputStream != null;
    }
}
