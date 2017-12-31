package id.unifi.service.core.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.detection.RawDetectionReport;
import id.unifi.service.common.detection.RawSiteDetectionReport;
import id.unifi.service.core.AgentSessionData;
import id.unifi.service.core.CoreService;
import static id.unifi.service.core.CoreService.PENDING_RAW_DETECTIONS_QUEUE_NAME;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@ApiService("detection")
public class DetectionService {
    private static final Logger log = LoggerFactory.getLogger(DetectionService.class);
    private Connection connection;

    public DetectionService(CoreService.MqConfig mqConfig) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(mqConfig.endpoint().getHost());
        factory.setPort(mqConfig.endpoint().getPort());
        try {
            connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        try (Channel channel = connection.createChannel()) {
            channel.queueDeclare(PENDING_RAW_DETECTIONS_QUEUE_NAME, true, false, false, null);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @ApiOperation
    public void processRawDetections(AgentSessionData session, ObjectMapper mapper, RawDetectionReport report) {
        log.trace("Got report: {}", report);
        RawSiteDetectionReport siteReport =
                new RawSiteDetectionReport(session.getClientId(), session.getSiteId(), report);

        byte[] marshalledReport;
        try {
            marshalledReport = mapper.writeValueAsBytes(siteReport);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        try (Channel channel = connection.createChannel()) {
            channel.basicPublish("", PENDING_RAW_DETECTIONS_QUEUE_NAME, MessageProperties.PERSISTENT_BASIC, marshalledReport);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}