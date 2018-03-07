package id.unifi.service.demo.gallagher;

import id.unifi.service.provider.security.gallagher.FtcApi;
import id.unifi.service.provider.security.gallagher.IFTMiddleware2;

import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;

public class GallagherDemo implements IFTMiddleware2 {

    private FtcApi ftcApi;
    public static final CountDownLatch registerLatch = new CountDownLatch(1);
    private static final CountDownLatch quitLatch = new CountDownLatch(1);

    public GallagherDemo(FtcApi ftcApi) {
        this.ftcApi = ftcApi;
    }

    public static void main(String[] args) throws Exception {

        System.out.println("********* GALLAGHER DEMO *********");

        FtcApi ftcApi = new FtcApi(
                "10.0.99.3",
                "localhost",
                "Administrator",
                "TestPass123");
        ftcApi.registerMiddleware(new GallagherDemo(ftcApi));

        registerLatch.await();

        ftcApi.logCardEvent(
                4, 1, ZonedDateTime.now(), false, 12345, 11111, "ES01", "ESI01",
                "Card detected: Zone [Reception], #12345", "No details.");

        quitLatch.await();

    }

    @Override
    public void notifyItemRegistered(String systemId, String itemId, String config) {
        ftcApi.notifyStatus("ES01", "ESI01", 1, false, false, "Unifi.id: Zone [Reception] is online.");
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
