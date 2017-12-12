package id.unifi.service.common.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEmailSender implements EmailSenderProvider {
    private static Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    public void send(String address, EmailMessage message) {
        log.info("Would send an email to " + address + ": " + message.subject + "\n" + message.htmlBody);
    }
}
