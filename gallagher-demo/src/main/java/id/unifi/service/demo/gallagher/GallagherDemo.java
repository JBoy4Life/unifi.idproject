package id.unifi.service.demo.gallagher;

import com.google.common.net.HostAndPort;
import com.impinj.octane.AntennaStatus;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.Settings;
import com.impinj.octane.Status;
import com.impinj.octane.TagReportListener;
import static java.util.stream.Collectors.toSet;

import org.jinterop.dcom.core.*;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class GallagherDemo {

    private static final Logger log = LoggerFactory.getLogger(GallagherDemo.class);
    public static final CountDownLatch registerLatch = new CountDownLatch(1);
    private static final CountDownLatch quitLatch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {

        final String GUID_FTCEAPI = "C56CA66E-FF0E-4581-B8F3-63F9725D2EC9";

//        InetAddress localAddress = InetAddress.getLocalHost();
//        //HostAndPort readerEndpoint = findReader(localAddress);
//        HostAndPort readerEndpoint = HostAndPort.fromParts("192.168.42.167", 5084);
//
//        log.info("Found reader {}, connecting...", readerEndpoint);
//        ImpinjReader reader = connectToReader(readerEndpoint);

        String serverAddress = "192.168.56.2";
        JISession session = JISession.createSession("localhost", "administrator", "TestPass123");
        JIComServer comServer = new JIComServer(JIClsid.valueOf(GUID_FTCEAPI), serverAddress, session);
        IJIComObject ftcApi = comServer.createInstance();

        JILocalCoClass middleware = createMiddleware();
        registerMiddleware(ftcApi, middleware);

//        startDetecting(reader, (r, report) -> report.getTags().forEach(t -> {
//            log.info("Detected tag: {}", t);
//        }));

        registerLatch.await();

        IJIComObject ftc = ftcApi.queryInterface("C06D6E49-9FE5-4C1C-8CFE-AA9763CCDDAB");
        logCardEvent(ftc);

        quitLatch.await();

    }


    private static JILocalCoClass createMiddleware() {

        final JILocalCoClass coClass = new JILocalCoClass(
                new JILocalInterfaceDefinition(Middleware.CLSID, false),
                Middleware.class);

        JILocalInterfaceDefinition interfaceDefinition =
                coClass.getInterfaceDefinition();
        interfaceDefinition.addMethodDescriptor(notifyItemRegisteredMD());
        interfaceDefinition.addMethodDescriptor(notifyItemDeregisteredMD());
        interfaceDefinition.addMethodDescriptor(notifySystemRegisteredMD());
        interfaceDefinition.addMethodDescriptor(notifySystemDeregisteredMD());
        interfaceDefinition.addMethodDescriptor(notifyAlarmAcknowledgedMD());

        ArrayList<String> supportedInterfaces = new ArrayList<>();
        supportedInterfaces.add("ED5CDD41-4393-4B88-BF30-F250905D57C8");
        coClass.setSupportedEventInterfaces(supportedInterfaces);

        return coClass;

    }

    private static String registerMiddleware(
            IJIComObject ftc,
            JILocalCoClass middleware
    ) throws Exception {

        IJIComObject remoteMiddleware = ftc.queryInterface(
                "304B34B7-69F0-4429-B96D-840431E1275C");
        return JIObjectFactory.attachEventHandler(
                remoteMiddleware,
                "ED5CDD41-4393-4B88-BF30-F250905D57C8",
                JIObjectFactory.buildObject(ftc.getAssociatedSession(), middleware));

    }

    private static JILocalMethodDescriptor notifyItemRegisteredMD() {
        final JILocalParamsDescriptor params = new JILocalParamsDescriptor();
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        return new JILocalMethodDescriptor("notifyItemRegistered", params);
    }

    private static JILocalMethodDescriptor notifyItemDeregisteredMD() {
        final JILocalParamsDescriptor params = new JILocalParamsDescriptor();
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        return new JILocalMethodDescriptor("notifyItemDeregistered", params);
    }

    private static JILocalMethodDescriptor notifySystemRegisteredMD() {
        final JILocalParamsDescriptor params = new JILocalParamsDescriptor();
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        return new JILocalMethodDescriptor("notifySystemRegistered", params);
    }

    private static JILocalMethodDescriptor notifySystemDeregisteredMD() {
        final JILocalParamsDescriptor params = new JILocalParamsDescriptor();
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        return new JILocalMethodDescriptor("notifySystemDeregistered", params);
    }

    private static JILocalMethodDescriptor notifyAlarmAcknowledgedMD() {
        final JILocalParamsDescriptor params = new JILocalParamsDescriptor();
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        params.addInParamAsObject(JIUnsignedInteger.class, JIFlags.FLAG_REPRESENTATION_UNSIGNED_INT);
        return new JILocalMethodDescriptor("notifyAlarmAcknowledged", params);
    }

    private static void logCardEvent(IJIComObject ftc) throws Exception {
        JICallBuilder cb = new JICallBuilder(true);
        cb.setOpnum(1);
        cb.addInParamAsInt(4, JIFlags.FLAG_NULL);         // lEventType
        cb.addInParamAsInt(1, JIFlags.FLAG_NULL);         // lEventID
        cb.addInParamAsDouble(40967.6424503935, JIFlags.FLAG_NULL);      // dEventTime
        cb.addInParamAsInt(0, JIFlags.FLAG_NULL);         // bHasRestoral
        cb.addInParamAsInt(12345, JIFlags.FLAG_NULL);     // lCardNumber
        cb.addInParamAsInt(11111, JIFlags.FLAG_NULL);     // lFacilityCode
        cb.addInParamAsString("ES01", JIFlags.FLAG_REPRESENTATION_STRING_BSTR);    // sSystemId
        cb.addInParamAsString("ESI01", JIFlags.FLAG_REPRESENTATION_STRING_BSTR);   // sItemID
        cb.addInParamAsString("message", JIFlags.FLAG_REPRESENTATION_STRING_BSTR); // sMessage
        cb.addInParamAsString("details", JIFlags.FLAG_REPRESENTATION_STRING_BSTR); // sDetails
        try {
            ftc.call(cb);
        } catch (Exception e) {
            throw e;
        }
    }

    private static void notifyStatus(IJIComObject ftc) throws Exception {
        JICallBuilder cb = new JICallBuilder(true);
        cb.setOpnum(3);
        cb.addInParamAsString("ES01", JIFlags.FLAG_REPRESENTATION_STRING_BSTR);    // sSystemId
        cb.addInParamAsString("ESI01", JIFlags.FLAG_REPRESENTATION_STRING_BSTR);   // sItemID
        cb.addInParamAsInt(0, JIFlags.FLAG_NULL);         // lState
        cb.addInParamAsBoolean(false, JIFlags.FLAG_NULL); // bTampered
        cb.addInParamAsBoolean(false, JIFlags.FLAG_NULL); // bOffline
        cb.addInParamAsString("", JIFlags.FLAG_REPRESENTATION_STRING_BSTR);   // sMessage
        ftc.call(cb);
    }

    private static ImpinjReader connectToReader(HostAndPort readerEndpoint) throws OctaneSdkException {
        ImpinjReader reader = new ImpinjReader();
        reader.connect(readerEndpoint.getHost(), readerEndpoint.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(reader::disconnect));

        Status status = reader.queryStatus();
        Set<Integer> connectedPorts = status.getAntennaStatusGroup().getAntennaList().stream()
                .filter(AntennaStatus::isConnected)
                .map(a -> (int) a.getPortNumber())
                .collect(toSet());
        log.info("Connected ports: {}", connectedPorts);
        return reader;
    }

    private static HostAndPort findReader(InetAddress localAddress) throws IOException {
        JmDNS jmdns = JmDNS.create(localAddress);

        ServiceInfo[] readerServices;
        do {
            log.info("Looking for LLRP readers on {}...", localAddress);
            readerServices = jmdns.list("_llrp._tcp.local.");
            log.info("Discovered readers: {}", Arrays.toString(readerServices));
        } while (readerServices.length == 0);

        ServiceInfo service = readerServices[0];
        String ipAddress = service.getInet4Addresses()[0].getHostName();
        int port = service.getPort();
        return HostAndPort.fromParts(ipAddress, port);
    }

    private static void startDetecting(ImpinjReader reader, TagReportListener listener) throws Exception {
        Settings settings = reader.queryDefaultSettings();

        settings.getLowDutyCycle().setIsEnabled(false);

        ReportConfig reportConfig = settings.getReport();
        reportConfig.setIncludeAntennaPortNumber(true);
        reportConfig.setIncludePeakRssi(true);
        reportConfig.setIncludeLastSeenTime(true);
        reportConfig.setMode(ReportMode.Individual);
        reader.applySettings(settings);

        reader.setTagReportListener(listener);
        reader.start();
    }
}
