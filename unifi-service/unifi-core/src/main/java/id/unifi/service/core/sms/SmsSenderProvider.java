package id.unifi.service.core.sms;

public interface SmsSenderProvider {
    void queue(String phoneNumber, String message, boolean promotional);
}
