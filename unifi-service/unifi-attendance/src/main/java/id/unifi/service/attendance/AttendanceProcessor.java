package id.unifi.service.attendance;

import com.google.common.collect.Iterables;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import static id.unifi.service.attendance.db.Attendance.ATTENDANCE;
import static id.unifi.service.attendance.db.Keys.ATTENDANCE_PKEY;
import static id.unifi.service.attendance.db.Keys.PROCESSING_STATE_PKEY;
import static id.unifi.service.attendance.db.Tables.ATTENDANCE_;
import static id.unifi.service.attendance.db.Tables.PROCESSING_STATE;
import id.unifi.service.common.detection.DetectionMatch;
import id.unifi.service.common.detection.DetectionMatchMqConsumer;
import id.unifi.service.common.mq.MqUtils;
import static id.unifi.service.common.mq.MqUtils.DETECTION_MATCH_TYPE;
import id.unifi.service.common.mq.Tagged;
import id.unifi.service.common.types.pk.AntennaPK;
import id.unifi.service.common.util.BatchBuffer;
import static id.unifi.service.core.db.Core.CORE;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;

public class AttendanceProcessor implements DetectionMatchMqConsumer {
    private static final Logger log = LoggerFactory.getLogger(AttendanceProcessor.class);
    private static final String PROCESSING_QUEUE_NAME = "attendance.detection.processing";

    private static final int BUFFER_SIZE = 10_000;
    private static final Duration BATCH_CREATION_TIMEOUT = Duration.ofSeconds(1);

    private final Database db;
    private final AttendanceMatcher attendanceMatcher;

    private static final Query insertAttendanceQuery = DSL.insertInto(ATTENDANCE_,
            ATTENDANCE_.CLIENT_ID, ATTENDANCE_.CLIENT_REFERENCE, ATTENDANCE_.SCHEDULE_ID, ATTENDANCE_.BLOCK_ID)
            .values((String) null, null, null, null)
            .onConflict(ATTENDANCE_PKEY.getFieldsArray()).doNothing();
    private static final Query insertProcessingStateQuery = DSL.insertInto(PROCESSING_STATE,
            PROCESSING_STATE.CLIENT_ID, PROCESSING_STATE.READER_SN, PROCESSING_STATE.PORT_NUMBER, PROCESSING_STATE.PROCESSED_UP_TO)
            .values((String) null, null, null, null)
            .onConflict(PROCESSING_STATE_PKEY.getFieldsArray())
            .doUpdate()
            .set(PROCESSING_STATE.PROCESSED_UP_TO, (Instant) null)
            .where(PROCESSING_STATE.PROCESSED_UP_TO.lt((Instant) null));

    public AttendanceProcessor(DatabaseProvider dbProvider, AttendanceMatcher attendanceMatcher) {
        this.db = dbProvider.bySchema(CORE, ATTENDANCE);
        this.attendanceMatcher = attendanceMatcher;
    }

    public void start(Connection connection, String exchangeName) throws IOException {
        var channel = connection.createChannel();
        channel.queueDeclare(PROCESSING_QUEUE_NAME, true, false, false, null);
        channel.queueBind(PROCESSING_QUEUE_NAME, exchangeName, "");

        var processingBuffer = BatchBuffer.<Tagged<DetectionMatch>>create(
                "attendance-processor", BUFFER_SIZE, BATCH_CREATION_TIMEOUT,
                detections -> processAttendance(channel, detections));

        var consumer = MqUtils.unmarshallingConsumer(DETECTION_MATCH_TYPE, channel, processingBuffer::put);
        channel.basicConsume(PROCESSING_QUEUE_NAME, consumer);

        attendanceMatcher.start();
    }

    private void processAttendance(Channel channel, List<Tagged<DetectionMatch>> detections) {
        if (detections.isEmpty()) return;

        log.debug("Processing {} detections", detections.size());
        var attendances = detections.stream()
                .map(d -> d.payload)
                .flatMap(attendanceMatcher::match)
                .collect(toSet());

        var newProcessingStates = detections.stream()
                .map(d -> d.payload.detection)
                .collect(toMap(
                        d -> new AntennaPK(d.detectable.clientId, d.readerSn, d.portNumber),
                        d -> d.detectionTime,
                        BinaryOperator.maxBy(Comparator.<Instant>naturalOrder())));

        if (!detections.isEmpty()) {
            db.execute(sql -> {
                if (!attendances.isEmpty()) {
                    var batch = sql.batch(insertAttendanceQuery);
                    for (var attendance : attendances) {
                        batch.bind(attendance.clientId, attendance.clientReference, attendance.scheduleId, attendance.blockId);
                    }

                    var attendanceRowsInserted = batch.execute();
                    var newAttendanceRows = Arrays.stream(attendanceRowsInserted).sum();
                    if (newAttendanceRows > 0) {
                        log.debug("Persisting: {}", attendances);
                        log.debug("Attendance persisted: {}, of which new: {}",
                                attendanceRowsInserted.length, newAttendanceRows);
                    }
                }

                var stateBatch = sql.batch(insertProcessingStateQuery);
                for (var entry : newProcessingStates.entrySet()) {
                    var antenna = entry.getKey();
                    var processedUpTo = entry.getValue();
                    stateBatch.bind(antenna.clientId, antenna.readerSn, antenna.portNumber,
                            processedUpTo, processedUpTo, processedUpTo);
                }
                var stateRowsInserted = stateBatch.execute();
                var updatedStateRows = Arrays.stream(stateRowsInserted).sum();
                if (updatedStateRows > 0) {
                    log.debug("New processing state: {}", newProcessingStates);
                }
                return null;
            });

            try {
                channel.basicAck(Iterables.getLast(detections).deliveryTag, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
