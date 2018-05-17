package id.unifi.service.core.agents;

import id.unifi.service.common.agent.AgentHealth;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.detection.SiteDetectionReport;
import id.unifi.service.core.AgentSessionData;
import id.unifi.service.core.detection.ReaderHealthContainer;
import id.unifi.service.core.processing.DetectionProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApiService("detection")
public class DetectionService {
    private static final Logger log = LoggerFactory.getLogger(DetectionService.class);
    private final DetectionProcessor detectionProcessor;
    private final ReaderHealthContainer readerHealthContainer;

    public DetectionService(DetectionProcessor detectionProcessor,
                            ReaderHealthContainer readerHealthContainer) {
        this.detectionProcessor = detectionProcessor;
        this.readerHealthContainer = readerHealthContainer;
    }

    @ApiOperation
    public void processRawDetections(AgentSessionData session, List<SiteDetectionReport> reports) {
        log.trace("Got reports: {}", reports);
        detectionProcessor.process(session.getAgent().clientId, reports);
    }

    @ApiOperation
    public void reportAgentHealth(AgentSessionData session, AgentHealth health) {
        readerHealthContainer.putHealth(session.getAgent(), health);
    }
}
