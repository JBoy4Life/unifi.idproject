package id.unifi.service.core.agent;

import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.types.client.ClientDetectable;
import id.unifi.service.common.types.pk.AntennaPK;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.detection.SiteRfidDetection;
import id.unifi.service.common.detection.SiteDetectionReport;
import id.unifi.service.core.agent.config.AgentFullConfig;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.DETECTABLE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class MockReaderManager implements ReaderManager {
    private static final Logger log = LoggerFactory.getLogger(MockReaderManager.class);

    private final String clientId;
    private final Consumer<SiteDetectionReport> detectionConsumer;
    private volatile Thread detectionThread;
    private volatile AntennaPK[] antennae;

    public MockReaderManager(AgentConfigPersistence persistence,
                             String clientId,
                             Consumer<SiteDetectionReport> detectionConsumer) {
        this.clientId = clientId;
        this.detectionConsumer = detectionConsumer;
    }

    public synchronized void configure(AgentFullConfig config) {
        log.info("Received reader config: {}", config);
        if (detectionThread != null) {
            detectionThread.interrupt();
            try {
                detectionThread.join();
            } catch (InterruptedException e) {
                return;
            }
        }

        antennae = config.readers.stream()
                .flatMap(r -> r.config.get().ports.get().keySet().stream().map(n -> new AntennaPK(clientId, r.readerSn.get(), n)))
                .toArray(AntennaPK[]::new);
        if (antennae.length > 0) {
            log.info("Generating mock detections for {} antennae", antennae.length);
            detectionThread = new Thread(this::mockDetections);
            detectionThread.start();
        } else {
            log.error("No antennae defined");
        }
    }

    private void mockDetections() {
        var random = new Random();
        var dbProvider = new DatabaseProvider();
        var serviceDb = dbProvider.bySchema(CORE);
        var detectables = serviceDb.execute(sql -> sql
                .selectFrom(DETECTABLE)
                .where(DETECTABLE.CLIENT_ID.eq(clientId))
                .and(DETECTABLE.DETECTABLE_TYPE.eq(DetectableType.UHF_EPC.toString()))
                .fetchArray());

        if (detectables.length == 0) {
            throw new RuntimeException("No detectables found in the database");
        }

        while (true) {
            var count = random.nextInt(10);
            for (var i = 0; i < count; i++) {
                var antenna = antennae[random.nextInt(antennae.length)];
                var detectableId = detectables[random.nextInt(detectables.length)].getDetectableId();
                var timestamp = Instant.now().minusMillis(random.nextInt(200));
                var detection =
                        new SiteRfidDetection(timestamp, antenna.portNumber, new ClientDetectable(detectableId, DetectableType.UHF_EPC), BigDecimal.ZERO, 1);
                detectionConsumer.accept(new SiteDetectionReport(antenna.readerSn, List.of(detection)));
            }

            try {
                Thread.sleep((long) (3000 + 3000 * Math.abs(random.nextGaussian())));
            } catch (InterruptedException e) {
                log.info("Mock detection thread interrupted, stopping");
                return;
            }
        }
    }
}
