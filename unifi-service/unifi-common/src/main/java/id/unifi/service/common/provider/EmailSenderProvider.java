package id.unifi.service.common.provider;

public interface EmailSenderProvider {
    class EmailMessage {
        public final String subject;
        public final String htmlBody;
        public final String textBody;

        public EmailMessage(String subject, String htmlBody, String textBody) {
            this.subject = subject;
            this.htmlBody = htmlBody;
            this.textBody = textBody;
        }
    }

    void queue(String fromAddress, String toName, String toAddress, EmailMessage message);

    void queue(String toName, String toAddress, EmailMessage message);
}
