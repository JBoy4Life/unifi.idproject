package id.unifi.service.core.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingSmsSenderProvider implements SmsSenderProvider {
    private static final Logger log = LoggerFactory.getLogger(LoggingSmsSenderProvider.class);

    public LoggingSmsSenderProvider() {
        log.info("Starting logging SMS provider");
    }

    public void queue(String phoneNumber, String message, boolean promotional) {
        log.info("Would send a {} SMS message to {}: {}",
                promotional ? "promotional" : "transactional", phoneNumber, message);
    }
}
