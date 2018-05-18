package id.unifi.service.mock.agent;

import com.statemachinesystems.envy.Default;
import com.statemachinesystems.envy.Envy;
import com.statemachinesystems.envy.Prefix;
import id.unifi.service.common.config.HexByteArrayValueParser;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.detection.SiteDetectionReport;
import id.unifi.service.common.detection.SiteRfidDetection;
import id.unifi.service.common.types.client.ClientDetectable;
import id.unifi.service.common.types.pk.AntennaPK;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.DETECTABLE;
import id.unifi.service.dbcommon.DatabaseProvider;
import static java.util.stream.Collectors.toUnmodifiableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

public class MockAgentService {
    private static final Logger log = LoggerFactory.getLogger(MockAgentService.class);
    @Prefix("unifi")
    interface Config {
        String clientId();

        @Default("default")
        String agentId();

        @Default("")
        byte[] agentPassword();

        @Default("ws://localhost:8001/agents/msgpack")
        URI serviceUri();
    }

    public static void main(String[] args) {
        var config = Envy.configure(Config.class, UnifiConfigSource.get(),
                HostAndPortValueParser.instance, HexByteArrayValueParser.instance);
        var clientId = config.clientId();
        var client = new SimpleCoreClient(config.serviceUri(), clientId, config.agentId(), config.agentPassword());
        var readerConfig = client.connect();
        var antennae = readerConfig.readers.stream()
                .flatMap(r -> r.config.get().ports.get().keySet().stream()
                        .map(n -> new AntennaPK(clientId, r.readerSn.get(), n)))
                .toArray(AntennaPK[]::new);
        var detectables = listDetectables(config.clientId());
        mockDetections(client, antennae, detectables);
    }

    private static List<ClientDetectable> listDetectables(String clientId) {
        var dbProvider = new DatabaseProvider();
        var serviceDb = dbProvider.bySchema(CORE);
        var detectables = serviceDb.execute(sql -> sql
                .selectFrom(DETECTABLE)
                .where(DETECTABLE.CLIENT_ID.eq(clientId))
                .stream()
                .map(r -> new ClientDetectable(r.getDetectableId(), DetectableType.fromString(r.getDetectableType())))
                .collect(toUnmodifiableList()));

        if (detectables.isEmpty()) {
            throw new RuntimeException("No detectables found in the database");
        }

        return detectables;
    }

    private static void mockDetections(SimpleCoreClient client,
                                       AntennaPK[] antennae,
                                       List<ClientDetectable> detectables) {
        log.info("Sending mock detections");
        var random = new Random();
        while (true) {
            var count = random.nextInt(10);
            var reports = IntStream.range(0, count).mapToObj(i -> {
                var antenna = antennae[random.nextInt(antennae.length)];
                var detectable = detectables.get(random.nextInt(detectables.size()));
                var timestamp = Instant.now().minusMillis(random.nextInt(200));
                var detection = new SiteRfidDetection(timestamp, antenna.portNumber, detectable, Optional.empty(), 1);
                return new SiteDetectionReport(antenna.readerSn, List.of(detection));
            }).collect(toUnmodifiableList());

            client.sendDetectionReports(reports);

            try {
                Thread.sleep((long) (3000 + 3000 * Math.abs(random.nextGaussian())));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
