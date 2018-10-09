package id.unifi.service.common.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import id.unifi.service.common.api.Protocol;
import static id.unifi.service.common.api.SerializationUtils.getObjectMapper;
import id.unifi.service.common.config.MqConfig;
import id.unifi.service.common.detection.DetectionMatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class MqUtils {
    private MqUtils() {}

    public static final TypeReference<DetectionMatch> DETECTION_MATCH_TYPE = new TypeReference<>() {};

    private static final ObjectMapper mapper = getObjectMapper(Protocol.MSGPACK);

    public static Connection connect(MqConfig mqConfig) {
        var factory = new ConnectionFactory();
        factory.setHost(mqConfig.endpoint().getHost());
        factory.setPort(mqConfig.endpoint().getPort());
        try {
            return factory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> com.rabbitmq.client.Consumer unmarshallingConsumer(TypeReference<T> type,
                                                                         Channel channel,
                                                                         InterruptibleConsumer<Tagged<T>> consumer) {
        return new DefaultConsumer(channel) {
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                var unmarshalled = unmarshal(body, type);
                try {
                    consumer.accept(new Tagged<>(unmarshalled, envelope.getDeliveryTag()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
    }

    public static <T> T unmarshal(byte[] body, TypeReference<T> type) throws IOException {
        return mapper.readValue(body, type);
    }

    public static <T> byte[] marshal(T value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> drainQueue(BlockingQueue<T> queue) {
        try {
            var size = queue.size();
            if (size <= 1) {
                var element = queue.take();
                return List.of(element);
            } else {
                List<T> taggedReports = new ArrayList<>(size);
                queue.drainTo(taggedReports, size);
                return taggedReports;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return List.of();
        }
    }

    public interface InterruptibleConsumer<T> {
        void accept(T t) throws InterruptedException;
    }
}
