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
import java.util.concurrent.TimeoutException;

public class MqUtils {
    private MqUtils() {}

    public static final String DETECTION_MATCH_EXCHANGE_NAME = "core.detection.detection-matches";
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
                T unmarshalled = mapper.readValue(body, type);
                try {
                    consumer.accept(new Tagged<>(unmarshalled, envelope.getDeliveryTag()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
    }

    public static <T> byte[] marshal(T value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public interface InterruptibleConsumer<T> {
        void accept(T t) throws InterruptedException;
    }
}
