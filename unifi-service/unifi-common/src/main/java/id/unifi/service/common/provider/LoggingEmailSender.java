package id.unifi.service.common.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEmailSender implements EmailSenderProvider {
    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    public void send(String name, String address, EmailMessage message) {
        log.info("Would send an email to {} <{}>: {}\n{}", name, address, message.subject, message.textBody);
    }
}
