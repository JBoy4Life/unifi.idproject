package id.unifi.service.core.web.handlers;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import java.util.Optional;

public class Contacts {
    static public class Contact {
        public final String clientId;
        public final String clientReference;

        @JsonCreator
        public Contact(String clientId, String clientReference) {
            this.clientId = clientId;
            this.clientReference = clientReference;
        }
    }

    public Contacts() {
        
    }

    public List<Contact> list(String clientId, Optional<List<Long>> numbers) {
        return List.of(new Contact(clientId, "alice"), new Contact(clientId, "bob"));
    }
}
