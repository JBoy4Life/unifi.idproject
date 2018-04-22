package id.unifi.service.provider.rfid;

import com.google.common.net.HostAndPort;
import com.impinj.octane.AntennaStatus;
import com.impinj.octane.FeatureSet;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Status;
import id.unifi.service.common.rfid.RfidReader;
import id.unifi.service.common.rfid.RfidReaderStatus;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class LlrpReaderDiscovery {
    private static final Logger log = LoggerFactory.getLogger(LlrpReaderDiscovery.class);
    private static final String IMPINJ_PEN = "25882";

    private static class ServiceSpec {
        final String name;
        final HostAndPort endpoint;

        ServiceSpec(String name, HostAndPort endpoint) {
            this.name = name;
            this.endpoint = endpoint;
        }

        public String toString() {
            return "ServiceSpec{" +
                    "name='" + name + '\'' +
                    ", endpoint=" + endpoint +
                    '}';
        }
    }

    private static boolean isAccessibleImpinjReader(ServiceInfo service) {
        return service.getInet4Addresses().length > 0
                && "reader".equals(service.getPropertyString("interface"))
                && IMPINJ_PEN.equals(service.getPropertyString("pen"));
    }

    public static List<RfidReader> discoverReaders(boolean logFeatures) {
        InetAddress localAddress;
        try {
            localAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Can't resolve local host name into an IP address.", e);
        }
        
        log.info("Looking for LLRP readers on {}...", localAddress);
        Stream<ServiceSpec> services;
        try (JmDNS jmdns = JmDNS.create(localAddress)) {
            services = Arrays.stream(jmdns.list("_llrp._tcp.local."))
                    .filter(LlrpReaderDiscovery::isAccessibleImpinjReader)
            .map(s -> new ServiceSpec(s.getName(),
                    HostAndPort.fromParts(s.getInetAddresses()[0].getHostName(), s.getPort())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return services.map(service -> {
            FeatureSet features;
            Status status;
            try {
                ImpinjReader reader = new ImpinjReader();
                reader.connect(service.endpoint.getHost(), service.endpoint.getPort());
                features = reader.queryFeatureSet();
                status = reader.queryStatus();
                reader.disconnect();
            } catch (OctaneSdkException e) {
                log.error("Error querying reader {}", service);
                features = null;
                status = null;
            }

            if (status != null) {
                String readerSn = features.getSerialNumber().replaceAll("-", "");
                Map<Integer, Boolean> antennaeConnected = status.getAntennaStatusGroup().getAntennaList().stream()
                        .collect(toMap(a -> (int) a.getPortNumber(), AntennaStatus::isConnected));

                if (logFeatures) {
                    log.info("Reader features ({}/{}/{}):", readerSn, service.endpoint, features.getModelName());
                    log.info("  Reader modes: {}", features.getReaderModes());
                    log.info("  Tx frequencies: {}", features.getTxFrequencies());
                    log.info("  Tx powers (per antenna): {}", features.getTxPowers().stream()
                            .map(e -> e.Dbm)
                            .collect(toList()));
                    log.info("  Rx sensitivities (per antenna): {}", features.getRxSensitivities().stream()
                            .map(e -> e.Dbm)
                            .collect(toList()));
                }

                return new RfidReader(readerSn, features.getModelName(),
                        new RfidReaderStatus(service.endpoint, features.getFirmwareVersion(), antennaeConnected));
            } else {
                return new RfidReader(null, null, new RfidReaderStatus(service.endpoint, null, Map.of()));
            }
        }).collect(toList());
    }
}
