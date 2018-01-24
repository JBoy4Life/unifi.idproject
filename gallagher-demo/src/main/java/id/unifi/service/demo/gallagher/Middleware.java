package id.unifi.service.demo.gallagher;

import org.jinterop.dcom.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Middleware {

    private static final Logger log = LoggerFactory.getLogger(Middleware.class);

    static final String CLSID = "ED5CDD41-4393-4B88-BF30-F250905D57C8";

    public Middleware() {

    }

    public void notifyItemRegistered(JIString systemId, JIString itemId, JIString config) {
        log.info("notifyItemRegistered: {}, {}, {}.", systemId, itemId, config);
        GallagherDemo.registerLatch.countDown();
    }

    public void notifyItemDeregistered(JIString systemId, JIString itemId) {
        log.info("notifyItemDeregistered: {}, {}.", systemId, itemId);
    }

    public void notifySystemRegistered(JIString systemId, JIString typeId, JIString config) {
        log.info("notifySystemRegistered: {}, {}, {}.", systemId, typeId, config);
    }

    public void notifySystemDeregistered(JIString systemId) {
        log.info("notifySystemDeregistered: {}.", systemId);
    }

    public void notifyAlarmAcknowledged(JIString systemId, JIUnsignedInteger eventId) {
        log.info("notifyAlarmAcknowledged: {}, {}.", systemId, eventId);
    }

}