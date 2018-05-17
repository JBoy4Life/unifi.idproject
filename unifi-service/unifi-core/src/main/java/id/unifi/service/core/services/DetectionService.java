package id.unifi.service.core.services;

import id.unifi.service.common.agent.ReaderHealth;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.detection.Detection;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.types.pk.DetectablePK;
import id.unifi.service.common.types.pk.OperatorPK;
import static id.unifi.service.common.util.TimeUtils.instantFromUtcLocal;
import static id.unifi.service.common.util.TimeUtils.utcLocalFromZoned;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.RFID_DETECTION;
import id.unifi.service.core.db.tables.records.RfidDetectionRecord;
import id.unifi.service.core.detection.ReaderHealthContainer;
import id.unifi.service.core.processing.DetectionProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@ApiService("detection")
public class DetectionService {
    private static Logger log = LoggerFactory.getLogger(DetectionService.class);

    private final Database db;
    private final DetectionProcessor detectionProcessor;
    private final ReaderHealthContainer readerHealthContainer;

    public DetectionService(DatabaseProvider dbProvider,
                            DetectionProcessor detectionProcessor,
                            ReaderHealthContainer readerHealthContainer) {
        this.db = dbProvider.bySchema(CORE);
        this.detectionProcessor = detectionProcessor;
        this.readerHealthContainer = readerHealthContainer;
    }

    // TODO: This should be accessible only to unifi.id staff
    @ApiOperation
    public void processFromDatabase(OperatorSessionData session,
                                    String clientId,
                                    ZonedDateTime startTime,
                                    ZonedDateTime endTime) {
        authorize(session, clientId);
        if (startTime.isAfter(endTime)) return;

        var detections = db.execute(sql -> sql.selectFrom(RFID_DETECTION)
                .where(RFID_DETECTION.DETECTION_TIME.ge(utcLocalFromZoned(startTime)))
                .and(RFID_DETECTION.DETECTION_TIME.lt(utcLocalFromZoned(endTime)))
                .and(RFID_DETECTION.CLIENT_ID.eq(clientId))
                .fetch(DetectionService::detectionFromRecord));

        log.info("Processing {} detections from DB", detections.size());
        detectionProcessor.process(detections);
    }

    @ApiOperation
    public List<ReaderHealth> getReaderHealth(OperatorSessionData session, String clientId) {
        authorize(session, clientId);

        return readerHealthContainer.getHealth(clientId);
    }

    private static Detection detectionFromRecord(RfidDetectionRecord r) {
        return new Detection(
                new DetectablePK(r.getClientId(), r.getDetectableId(), DetectableType.fromString(r.getDetectableType())),
                r.getReaderSn(),
                r.getPortNumber(),
                instantFromUtcLocal(r.getDetectionTime()),
                Optional.ofNullable(r.getRssi()),
                r.getCount());
    }

    private static OperatorPK authorize(OperatorSessionData sessionData, String clientId) {
        return Optional.ofNullable(sessionData.getOperator())
                .filter(op -> op.clientId.equals(clientId))
                .orElseThrow(Unauthorized::new);
    }
}
