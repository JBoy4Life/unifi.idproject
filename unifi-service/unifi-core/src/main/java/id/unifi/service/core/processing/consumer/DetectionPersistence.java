package id.unifi.service.core.processing.consumer;

import com.google.common.collect.Iterables;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import id.unifi.service.common.detection.DetectionMatch;
import id.unifi.service.common.detection.DetectionMatchMqConsumer;
import id.unifi.service.common.mq.MqUtils;
import static id.unifi.service.common.mq.MqUtils.DETECTION_MATCH_TYPE;
import id.unifi.service.common.mq.Tagged;
import id.unifi.service.common.util.BatchBuffer;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.DETECTABLE;
import static id.unifi.service.core.db.Tables.RFID_DETECTION;
import static id.unifi.service.dbcommon.DatabaseUtils.CITEXT;
import static id.unifi.service.dbcommon.DatabaseUtils.unqualified;
import org.jooq.Field;
import org.jooq.Row8;
import static org.jooq.impl.DSL.*;
import static org.jooq.util.postgres.PostgresDataType.DECIMAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class DetectionPersistence implements DetectionMatchMqConsumer {
    private static final Logger log = LoggerFactory.getLogger(DetectionPersistence.class);
    private static final String PERSISTENCE_QUEUE_NAME = "core.detection.persistence";

    private static final Field<String> CLIENT_ID = unqualified(RFID_DETECTION.CLIENT_ID);
    private static final Field<String> DETECTABLE_ID = unqualified(RFID_DETECTION.DETECTABLE_ID);
    private static final Field<DetectableType> DETECTABLE_TYPE = unqualified(RFID_DETECTION.DETECTABLE_TYPE);

    private static final String[] RFID_DETECTION_FIELD_NAMES =
            RFID_DETECTION.fieldStream().map(Field::getName).toArray(String[]::new);

    private static final int BUFFER_SIZE = 10_000;
    private static final Duration BATCH_CREATION_TIMEOUT = Duration.ofSeconds(1);
    private static final Duration INITIAL_RETRY_INTERVAL = Duration.ofMillis(500);
    private static final Duration MAX_RETRY_INTERVAL = Duration.ofSeconds(10);
    private static final Duration LOCK_TIMEOUT = Duration.ofMillis(2000);

    private final Database db;

    public DetectionPersistence(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
    }

    public void start(Connection connection, String exchangeName) throws IOException {
        var channel = connection.createChannel();
        channel.queueDeclare(PERSISTENCE_QUEUE_NAME, true, false, false, null);
        channel.queueBind(PERSISTENCE_QUEUE_NAME, exchangeName, "");

        var processingBuffer = BatchBuffer.<Tagged<DetectionMatch>>create(
                "persistence", BUFFER_SIZE, BATCH_CREATION_TIMEOUT,
                detections -> persist(channel, detections));

        var consumer = MqUtils.unmarshallingConsumer(DETECTION_MATCH_TYPE, channel, processingBuffer::put);
        channel.basicConsume(PERSISTENCE_QUEUE_NAME, consumer);
    }

    private void persist(Channel channel, List<Tagged<DetectionMatch>> taggedMatches) {
        if (taggedMatches.isEmpty()) return;

        @SuppressWarnings("unchecked")
        Row8<String, String, DetectableType, String, Integer, Instant, BigDecimal, Integer>[] rows = taggedMatches.stream()
                .map(d -> d.payload.detection)
                .map(d -> row(
                        cast(d.detectable.clientId, CITEXT),
                        cast(d.detectable.detectableId, CITEXT),
                        d.detectable.detectableType,
                        d.readerSn,
                        d.portNumber,
                        d.detectionTime,
                        cast(d.rssi.orElse(null), DECIMAL),
                        d.count))
                .toArray(Row8[]::new);

        var values = values(rows).asTable("v", RFID_DETECTION_FIELD_NAMES);

        var success = false;
        var retryIntervalMillis = INITIAL_RETRY_INTERVAL.toMillis();
        do {
            try {
                int rowsInserted = db.execute(sql -> {
                    sql.query(String.format("SET LOCAL lock_timeout = '%dms'", LOCK_TIMEOUT.toMillis())).execute();
                    return sql.insertInto(RFID_DETECTION,
                            RFID_DETECTION.CLIENT_ID,
                            RFID_DETECTION.DETECTABLE_ID,
                            RFID_DETECTION.DETECTABLE_TYPE,
                            RFID_DETECTION.READER_SN,
                            RFID_DETECTION.PORT_NUMBER,
                            RFID_DETECTION.DETECTION_TIME,
                            RFID_DETECTION.RSSI,
                            RFID_DETECTION.COUNT)
                            .select(selectFrom(values)
                                    .whereExists(selectOne().from(DETECTABLE)
                                            .where(DETECTABLE.CLIENT_ID.eq(values.field(CLIENT_ID)))
                                            .and(DETECTABLE.DETECTABLE_ID.eq(values.field(DETECTABLE_ID)))
                                            .and(DETECTABLE.DETECTABLE_TYPE.eq(values.field(DETECTABLE_TYPE)))))
                            .onConflictDoNothing()
                            .execute();
                });
                log.debug("Persisted {} new detections", rowsInserted);
                success = true;
            } catch (DataIntegrityViolationException | ConcurrencyFailureException e) {
                log.warn("Failed to insert detections, retrying in {} ms", retryIntervalMillis, e);
                try {
                    Thread.sleep(retryIntervalMillis);
                    retryIntervalMillis = Math.min(MAX_RETRY_INTERVAL.toMillis(), retryIntervalMillis * 2);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        } while (!success);

        var deliveryTag = Iterables.getLast(taggedMatches).deliveryTag;
        try {
            channel.basicAck(deliveryTag, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
