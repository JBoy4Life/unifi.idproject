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
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Set;

public class GallagherDemo {
    private static final Logger log = LoggerFactory.getLogger(GallagherDemo.class);

    public static void main(String[] args) throws Exception {

//        InetAddress localAddress = InetAddress.getLocalHost();
//        //HostAndPort readerEndpoint = findReader(localAddress);
//        HostAndPort readerEndpoint = HostAndPort.fromParts("192.168.42.167", 5084);
//
//        log.info("Found reader {}, connecting...", readerEndpoint);
//        ImpinjReader reader = connectToReader(readerEndpoint);

        String serverAddress = "192.168.56.2";
        JISession session = JISession.createSession("localhost", "administrator", "TestPass123");
        JIComServer ftceapi = new JIComServer(JIClsid.valueOf("C56CA66E-FF0E-4581-B8F3-63F9725D2EC9"), serverAddress, session); // FTCEAPI

        IJIComObject ftcApi = ftceapi.createInstance();
//        IJIComObject ftc  = ftcApi.queryInterface("304B34B7-69F0-4429-B96D-840431E1275C"); // IFTExternalEventInput
        IJIComObject ftc  = ftcApi.queryInterface("C06D6E49-9FE5-4C1C-8CFE-AA9763CCDDAB"); // IFTExternalEventInput2

//        IJIDispatch dispatch = (IJIDispatch)JIObjectFactory.narrowObject(ftcApi.queryInterface(IJIDispatch.IID));
//        int opnum = dispatch.getIDsOfNames("logCardEvent");

        JICallBuilder cb = new JICallBuilder(true);
        cb.setOpnum(1);
        cb.addInParamAsInt(4, JIFlags.FLAG_NULL);         // lEventType
        cb.addInParamAsInt(1, JIFlags.FLAG_NULL);         // lEventID
        cb.addInParamAsDouble(0, JIFlags.FLAG_NULL);      // dEventTime
        cb.addInParamAsBoolean(false, JIFlags.FLAG_NULL); // bHasRestoral
        cb.addInParamAsInt(12345, JIFlags.FLAG_NULL);     // lCardNumber
        cb.addInParamAsInt(11111, JIFlags.FLAG_NULL);     // lFacilityCode
        cb.addInParamAsString("ES01", JIFlags.FLAG_REPRESENTATION_STRING_BSTR);    // sSystemId
        cb.addInParamAsString("ESI01", JIFlags.FLAG_REPRESENTATION_STRING_BSTR);   // sItemID
        cb.addInParamAsString("message", JIFlags.FLAG_REPRESENTATION_STRING_BSTR); // sMessage
        cb.addInParamAsString("details", JIFlags.FLAG_REPRESENTATION_STRING_BSTR); // sDetails
        ftc.call(cb);

//        startDetecting(reader, (r, report) -> report.getTags().forEach(t -> {
//            log.info("Detected tag: {}", t);
//        }));

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
