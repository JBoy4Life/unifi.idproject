package id.unifi.service.provider.security.gallagher;

import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIUnsignedInteger;

public class IFTMiddleware2DcomBridge {

    private final IFTMiddleware2 middleware;

    public static final String CLSID = "ED5CDD41-4393-4B88-BF30-F250905D57C8";

    IFTMiddleware2DcomBridge(IFTMiddleware2 middleware) {
        this.middleware = middleware;
    }

    public void notifyItemRegistered(JIString systemId, JIString itemId, JIString config) {
        this.middleware.notifyItemRegistered(
                systemId.toString(),
                itemId.toString(),
                config.toString());
    }

    public void notifyItemDeregistered(JIString systemId, JIString itemId) {
        this.middleware.notifyItemDeregistered(
                systemId.toString(),
                itemId.toString());
    }

    public void notifySystemRegistered(JIString systemId, JIString typeId, JIString config) {
        this.middleware.notifySystemRegistered(
                systemId.toString(),
                typeId.toString(),
                config.toString());
    }

    public void notifySystemDeregistered(JIString systemId) {
        this.middleware.notifySystemDeregistered(
                systemId.toString());
    }

    public void notifyAlarmAcknowledged(JIString systemId, JIUnsignedInteger eventId) {
        this.middleware.notifyAlarmAcknowledged(
                systemId.toString(),
                eventId.getType());
    }

}