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
import org.jinterop.dcom.core.JIClsid;
import org.jinterop.dcom.core.JIComServer;
import org.jinterop.dcom.core.JIProgId;
import org.jinterop.dcom.core.JISession;
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
        InetAddress localAddress = InetAddress.getLocalHost();
        //HostAndPort readerEndpoint = findReader(localAddress);
        HostAndPort readerEndpoint = HostAndPort.fromParts("192.168.42.167", 5084);

        log.info("Found reader {}, connecting...", readerEndpoint);
        ImpinjReader reader = connectToReader(readerEndpoint);

        String serverAddress = "192.168.42.5";
        JISession session = JISession.createSession("localhost", "administrator", "MacFUCK69");
        JIComServer comServer = new JIComServer(JIProgId.valueOf("CardaxFTEIAPI"), serverAddress, session);
        //JIComServer comServer = new JIComServer(JIProgId.valueOf("CardaxFTCAPI"), serverAddress, session);
        //JIComServer comServer = new JIComServer(JIClsid.valueOf("70C694E6-8A64-48B0-A58E-A7766C28B7C9"), serverAddress, session);

        startDetecting(reader, (r, report) -> report.getTags().forEach(t -> {
            log.info("Detected tag: {}", t);
        }));
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
