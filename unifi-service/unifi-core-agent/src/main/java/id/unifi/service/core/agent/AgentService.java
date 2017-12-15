package id.unifi.service.core.agent;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.net.HostAndPort;
import com.statemachinesystems.envy.Default;
import id.unifi.service.common.rfid.RfidReader;
import id.unifi.service.provider.rfid.LlrpReaderDiscovery;
import id.unifi.service.provider.rfid.RfidDetectionReport;
import id.unifi.service.provider.rfid.RfidProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class AgentService {
    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    interface Config {
        @Default("127.0.0.1")
        String rabbitMqHost();

        @Default("5672")
        int rabbitMqPort();

        String clientId();
    }

    public static void main(String[] args) throws Exception {
        BlockingQueue<RfidDetectionReport> detectionQueue = new ArrayBlockingQueue<>(1000);
        RfidProvider rfidProvider = new RfidProvider(detectionQueue::add);

        List<RfidReader> readers = LlrpReaderDiscovery.discoverReaders();

        for (RfidReader reader : readers) {
            System.out.println(reader);
        }

        RfidReader reader1 = readers.get(0);
        Map<String, HostAndPort> endpoints = Map.of(reader1.getSerialNumber(), reader1.getStatus().getEndpoint());

        Multimap<String, Integer> enabledAntennaPorts = HashMultimap.create();
        enabledAntennaPorts.put(reader1.getSerialNumber(), 1);
        rfidProvider.startDetecting(enabledAntennaPorts, endpoints);

        log.info("Listening for detections");
        while (true) {
            RfidDetectionReport report = detectionQueue.poll(10, TimeUnit.SECONDS);
            log.info(report.getDetections().toString());
        }
    }
}
