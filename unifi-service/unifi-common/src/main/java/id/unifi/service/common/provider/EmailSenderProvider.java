package id.unifi.service.common.provider;

public interface EmailSenderProvider {
    class EmailMessage {
        public final String subject;
        public final String htmlBody;

        public EmailMessage(String subject, String htmlBody) {
            this.subject = subject;
            this.htmlBody = htmlBody;
        }
    }

    void send(String address, EmailMessage message);
}
