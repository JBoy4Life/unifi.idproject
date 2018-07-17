package id.unifi.service.core.agent;

import com.codahale.metrics.MetricRegistry;
import id.unifi.service.common.detection.SiteDetectionReport;
import id.unifi.service.common.detection.SiteRfidDetection;
import id.unifi.service.core.agent.config.AgentConfig;
import id.unifi.service.core.agent.config.AgentFullConfig;
import id.unifi.service.core.agent.config.ConfigAdapter;
import id.unifi.service.core.agent.consumer.DetectionConsumer;
import id.unifi.service.core.agent.consumer.SiteDetectionReportConsumer;
import id.unifi.service.core.agent.logger.DetectionLogger;
import id.unifi.service.core.agent.rollup.RollupUtils;
import id.unifi.service.provider.rfid.RfidProvider;
import static java.util.stream.Collectors.toList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class CoreAgent {
    private static final Logger log = LoggerFactory.getLogger(CoreAgent.class);
    private final MetricRegistry registry;
    private final Consumer<SiteDetectionReport> rolledUpConsumer;
    private final BlockingQueue<AgentFullConfig> configQueue;
    private final Thread configThread;
    private final DetectionConsumer detectionConsumer;

    private volatile State state; // init (-> configuring -> running -> stopping)*
    private RfidProvider rfidProvider;

    public enum State {
        INIT, CONFIGURING, RUNNING, STOPPING
    }

    public static CoreAgent create(Optional<Function<ConfigAdapter, CoreClient>> coreClientFactory,
                                   DetectionLogger detectionLogger,
                                   MetricRegistry registry,
                                   Function<SiteDetectionReportConsumer, DetectionConsumer> detectionConsumerFactory) {
        var agent = new CoreAgent(coreClientFactory, detectionLogger, registry, detectionConsumerFactory);
        agent.configThread.start();
        return agent;
    }

    private CoreAgent(Optional<Function<ConfigAdapter, CoreClient>> coreClientFactory,
                      DetectionLogger detectionLogger,
                      MetricRegistry registry,
                      Function<SiteDetectionReportConsumer, DetectionConsumer> detectionConsumerFactory) {
        var coreClient = coreClientFactory.map(factory -> factory.apply(this::configure));
        this.detectionConsumer = coreClient.isPresent()
                ? detectionConsumerFactory.apply(coreClient.get()::sendDetectionReports)
                : report -> {};
        this.state = State.INIT;
        this.registry = registry;
        this.rfidProvider = null;
        this.rolledUpConsumer = report -> {
            detectionConsumer.accept(report);
            detectionLogger.log(report);
        };
        this.configQueue = new ArrayBlockingQueue<>(1);
        this.configThread = new Thread(this::runConfigLoop);
    }

    private void runConfigLoop() {
        AgentFullConfig config = null;
        try {
            while (true) {
                switch (state) {
                    case INIT:
                        config = configQueue.take();
                        transitionTo(State.CONFIGURING);
                        break;

                    case CONFIGURING:
                        if (config == null) throw new IllegalStateException("Config must not be null");
                        configureFully(config);
                        transitionTo(State.RUNNING);
                        break;

                    case RUNNING:
                        config = configQueue.take();
                        transitionTo(State.STOPPING);
                        break;

                    case STOPPING:
                        rfidProvider.close();
                        transitionTo(State.CONFIGURING);
                        break;
                }
            }
        } catch (InterruptedException e) {
            // TODO: Consider handling properly
        }
    }

    synchronized void configure(AgentFullConfig config, boolean authoritative) {
        // Apply config to RFID provider only if it's authoritative (i.e. from the server) or we timed out
        // and are applying previously persisted backup config.
        if (state != State.INIT && !authoritative) return;
        while (!configQueue.offer(config)) { // Make sure we only leave in only one pending config
            configQueue.poll();
        }
    }

    public State getState() {
        return state;
    }

    private void configureFully(AgentFullConfig config) {
        var agentConfig = config.agent.orElse(AgentConfig.empty);
        var detectableTypeFilter = agentConfig.detectableTypes
                .<Predicate<SiteRfidDetection>>map(types -> det -> types.contains(det.detectable.detectableType))
                .orElse(det -> true);

        var rollup = RollupUtils.rollupFromConfig(agentConfig.rollup);

        Consumer<SiteDetectionReport> consumer = report -> {
            var filteredReport = new SiteDetectionReport(
                    report.readerSn,
                    report.detections.stream().filter(detectableTypeFilter).collect(toList()));

            rollup.process(filteredReport).forEach(rolledUpConsumer);
        };

        rfidProvider = new RfidProvider(config.readers, consumer, registry);
    }

    private void transitionTo(State newState) {
        log.info("{} -> {}", state, newState);
        state = newState;
    }
}
