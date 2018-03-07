package id.unifi.service.provider.security.gallagher;

import java.time.ZonedDateTime;

interface IFTExternalEventInput3 extends IFTExternalEventInput2 {

    void logLongCardEvent(
            int eventType,
            int eventId,
            ZonedDateTime eventTime,
            boolean hasRestoral,
            int cardIdSize,
            byte[] cardId,
            int facilityCode,
            String systemId,
            String itemId,
            String message,
            String details
    );

    void logLongCardEvent2(
            int eventType,
            int eventId,
            ZonedDateTime eventTime,
            boolean hasRestoral,
            int cardNumberFormatType,
            String cardId,
            int facilityCode,
            String systemId,
            String itemId,
            String message,
            String details
    );

}
