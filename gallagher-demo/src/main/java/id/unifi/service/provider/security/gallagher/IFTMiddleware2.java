package id.unifi.service.provider.security.gallagher;

public interface IFTMiddleware2 {
    void notifyItemRegistered(String systemId, String itemId, String config);
    void notifyItemDeregistered(String systemId, String itemId);
    void notifySystemRegistered(String systemId, String typeId, String config);
    void notifySystemDeregistered(String systemId);
    void notifyAlarmAcknowledged(String systemId, int eventId);
}
