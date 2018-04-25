package id.unifi.service.provider.security.gallagher;

import java.time.Instant;

interface IFTExternalEventInput2 {

    void logEvent(
            int eventType,
            int eventId,
            Instant eventTime,
            boolean hasRestoral,
            String systemId,
            String itemId,
            String message,
            String details
    );

    void logCardEvent(
            int eventType,
            int eventId,
            Instant eventTime,
            boolean hasRestoral,
            int cardNumber,
            int facilityCode,
            String systemId,
            String itemId,
            String message,
            String details
    );

    void notifyRestore(
            int eventType,
            String systemId,
            String itemId
    );

    void notifyStatus(
            String systemId,
            String itemId,
            int state,
            boolean tampered,
            boolean offline,
            String message
    );

}
