package id.unifi.service.core.services;

import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.detection.Detection;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.types.pk.DetectablePK;
import id.unifi.service.common.types.pk.OperatorPK;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.RFID_DETECTION;
import id.unifi.service.core.db.tables.records.RfidDetectionRecord;
import id.unifi.service.core.processing.DetectionProcessor;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Optional;

@ApiService("detection")
public class DetectionService {
    private static Logger log = LoggerFactory.getLogger(DetectionService.class);

    private final Database db;
    private final DetectionProcessor detectionProcessor;

    public DetectionService(DatabaseProvider dbProvider, DetectionProcessor detectionProcessor) {
        this.db = dbProvider.bySchema(CORE);
        this.detectionProcessor = detectionProcessor;
    }

    // TODO: This should be accessible only to unifi.id staff
    @ApiOperation
    public void processFromDatabase(OperatorSessionData session,
                                    String clientId,
                                    OffsetDateTime startTime,
                                    OffsetDateTime endTime) {
        authorize(session, clientId);
        if (startTime.isAfter(endTime)) return;

        var detections = db.execute(sql -> sql.selectFrom(RFID_DETECTION)
                .where(RFID_DETECTION.DETECTION_TIME.ge(startTime.toInstant()))
                .and(RFID_DETECTION.DETECTION_TIME.lt(endTime.toInstant()))
                .and(RFID_DETECTION.CLIENT_ID.eq(clientId))
                .fetch(DetectionService::detectionFromRecord));

        log.info("Processing {} detections from DB", detections.size());
        detectionProcessor.process(detections);
    }

    private static Detection detectionFromRecord(RfidDetectionRecord r) {
        return new Detection(
                new DetectablePK(r.getClientId(), r.getDetectableId(), DetectableType.fromString(r.getDetectableType())),
                r.getReaderSn(),
                r.getPortNumber(),
                r.getDetectionTime(),
                Optional.ofNullable(r.getRssi()),
                r.getCount());
    }

    private static OperatorPK authorize(OperatorSessionData sessionData, String clientId) {
        return Optional.ofNullable(sessionData.getOperator())
                .filter(op -> op.clientId.equals(clientId))
                .orElseThrow(Unauthorized::new);
    }
}
