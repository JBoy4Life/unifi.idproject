package id.unifi.service.core.agent;

import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.detection.ReaderConfig;
import static id.unifi.service.core.agent.db.CoreAgent.CORE_AGENT;
import id.unifi.service.core.agent.db.Tables;
import id.unifi.service.core.agent.db.tables.records.AntennaRecord;
import id.unifi.service.core.agent.db.tables.records.ReaderRecord;
import id.unifi.service.provider.rfid.RfidProvider;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DefaultReaderManager implements ReaderManager {
    private static final Logger log = LoggerFactory.getLogger(DefaultReaderManager.class);

    private final Database db;
    private final RfidProvider rfidProvider;

    public DefaultReaderManager(DatabaseProvider dbProvider, RfidProvider rfidProvider) {
        this.db = dbProvider.bySchema(CORE_AGENT);
        this.rfidProvider = rfidProvider;
    }

    public synchronized void configure(List<ReaderConfig> readers) {
        persistConfig(readers);
        rfidProvider.configure(readers);
    }

    private void persistConfig(List<ReaderConfig> readers) {
        db.execute(sql -> {
            List<ReaderRecord> readerRecords = readers.stream()
                    .map(r -> new ReaderRecord(r.readerSn, r.endpoint.toString()))
                    .collect(toList());
            List<AntennaRecord> antennaRecords = readers.stream()
                    .flatMap(r -> stream(r.enabledAntennae).mapToObj(portNum -> new AntennaRecord(r.readerSn, portNum)))
                    .collect(toList());

            sql.truncate(Tables.READER).cascade().execute();

            sql.batchInsert(readerRecords).execute();
            sql.batchInsert(antennaRecords).execute();
            return null;
        });
    }
}
