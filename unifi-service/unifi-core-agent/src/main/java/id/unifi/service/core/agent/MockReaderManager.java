package id.unifi.service.core.agent;

import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.detection.AntennaKey;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.detection.RawDetection;
import id.unifi.service.common.detection.RawDetectionReport;
import id.unifi.service.core.agent.config.AgentFullConfig;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.DETECTABLE;
import id.unifi.service.core.db.tables.records.DetectableRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class MockReaderManager implements ReaderManager {
    private static final Logger log = LoggerFactory.getLogger(MockReaderManager.class);

    private final String clientId;
    private final Consumer<RawDetectionReport> detectionConsumer;
    private volatile Thread detectionThread;
    private volatile AntennaKey[] antennae;

    public MockReaderManager(AgentConfigPersistence persistence,
                             String clientId,
                             Consumer<RawDetectionReport> detectionConsumer) {
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
                .flatMap(r -> r.config.get().ports.get().keySet().stream().map(n -> new AntennaKey(clientId, r.readerSn.get(), n)))
                .toArray(AntennaKey[]::new);
        if (antennae.length > 0) {
            log.info("Generating mock detections for {} antennae", antennae.length);
            detectionThread = new Thread(this::mockDetections);
            detectionThread.start();
        } else {
            log.error("No antennae defined");
        }
    }

    private void mockDetections() {
        Random random = new Random();
        DatabaseProvider dbProvider = new DatabaseProvider();
        Database serviceDb = dbProvider.bySchema(CORE);
        DetectableRecord[] detectables = serviceDb.execute(sql -> sql
                .selectFrom(DETECTABLE)
                .where(DETECTABLE.CLIENT_ID.eq(clientId))
                .and(DETECTABLE.DETECTABLE_TYPE.eq(DetectableType.UHF_EPC.toString()))
                .fetchArray());

        if (detectables.length == 0) {
            throw new RuntimeException("No detectables found in the database");
        }

        while (true) {
            int count = random.nextInt(10);
            for (int i = 0; i < count; i++) {
                AntennaKey antenna = antennae[random.nextInt(antennae.length)];
                String detectableId = detectables[random.nextInt(detectables.length)].getDetectableId();
                Instant timestamp = Instant.now().minusMillis(random.nextInt(200));
                RawDetection detection =
                        new RawDetection(timestamp, antenna.portNumber, detectableId, DetectableType.UHF_EPC, 0d);
                detectionConsumer.accept(new RawDetectionReport(antenna.readerSn, List.of(detection)));
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
