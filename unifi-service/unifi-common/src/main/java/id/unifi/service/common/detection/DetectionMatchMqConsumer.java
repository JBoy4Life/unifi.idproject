package id.unifi.service.common.detection;

import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

// FIXME: Break away unifi-attendance and move this class to unifi-core
public interface DetectionMatchMqConsumer {
    void start(Connection connection, String exchangeName) throws IOException, TimeoutException;
}
