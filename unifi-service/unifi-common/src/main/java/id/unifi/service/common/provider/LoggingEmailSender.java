package id.unifi.service.common.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEmailSender implements EmailSenderProvider {
    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    public void queue(String fromAddress, String toName, String toAddress, EmailMessage message) {
        log.info("Would send an email from {} to {} <{}>: {}\n{}",
                fromAddress, toName, toAddress, message.subject, message.textBody);
    }

    public void queue(String toName, String toAddress, EmailMessage message) {
        log.info("Would send an email to {} <{}>: {}\n{}",
                toName, toAddress, message.subject, message.textBody);
    }
}
