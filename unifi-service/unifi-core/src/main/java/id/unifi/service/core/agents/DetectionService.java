package id.unifi.service.core.agents;

import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.detection.SiteDetectionReport;
import id.unifi.service.core.AgentSessionData;
import id.unifi.service.core.processing.DetectionProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApiService("detection")
public class DetectionService {
    private static final Logger log = LoggerFactory.getLogger(DetectionService.class);
    private final DetectionProcessor detectionProcessor;

    public DetectionService(DetectionProcessor detectionProcessor) {
        this.detectionProcessor = detectionProcessor;
    }

    @ApiOperation
    public void processRawDetections(AgentSessionData session, List<SiteDetectionReport> reports) {
        log.trace("Got reports: {}", reports);
        detectionProcessor.process(session.getAgent().clientId, reports);
    }
}
