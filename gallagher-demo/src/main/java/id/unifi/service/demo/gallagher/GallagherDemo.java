package id.unifi.service.demo.gallagher;

import id.unifi.service.provider.security.gallagher.FTCAPI;
import id.unifi.service.provider.security.gallagher.IFTMiddleware2;
import org.jinterop.dcom.core.JILocalCoClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class GallagherDemo implements IFTMiddleware2 {

    private FTCAPI ftcapi;
    private static final Logger log = LoggerFactory.getLogger(GallagherDemo.class);
    public static final CountDownLatch registerLatch = new CountDownLatch(1);
    private static final CountDownLatch quitLatch = new CountDownLatch(1);

    public GallagherDemo(FTCAPI ftcapi) {
        this.ftcapi = ftcapi;
    }

    public static void main(String[] args) throws Exception {

        System.out.println("********* GALLAGHER DEMO *********");

        FTCAPI ftcapi = new FTCAPI(
                "10.0.99.3",
                "localhost",
                "Administrator",
                "TestPass123");
        ftcapi.registerMiddleware(new GallagherDemo(ftcapi));

        registerLatch.await();

        ftcapi.logCardEvent(
                4, 1, new Date(), false, 12345, 11111, "ES01", "ESI01",
                "Card detected: Zone [Reception], #12345", "No details.");

        quitLatch.await();

    }

    @Override
    public void notifyItemRegistered(String systemId, String itemId, String config) {
        ftcapi.notifyStatus(systemId, itemId, 1, false, false, "Unifi.id: Zone [Reception] is online.");
        registerLatch.countDown();
    }

    @Override
    public void notifyItemDeregistered(String systemId, String itemId) {

    }

    @Override
    public void notifySystemRegistered(String systemId, String typeId, String config) {

    }

    @Override
    public void notifySystemDeregistered(String systemId) {

    }

    @Override
    public void notifyAlarmAcknowledged(String systemId, int eventId) {
    }

}
