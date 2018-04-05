package id.unifi.service.core.agent;

import com.google.common.net.HostAndPort;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.detection.ReaderConfig;
import static id.unifi.service.core.agent.db.CoreAgent.CORE_AGENT;
import static id.unifi.service.core.agent.db.Tables.ANTENNA;
import static id.unifi.service.core.agent.db.Tables.READER;
import id.unifi.service.core.agent.db.tables.records.AntennaRecord;
import id.unifi.service.core.agent.db.tables.records.ReaderRecord;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ReaderConfigDatabasePersistence implements ReaderConfigPersistence {
    private final Database db;
    private final List<ReaderConfig> alternativeConfig;

    public ReaderConfigDatabasePersistence(DatabaseProvider dbProvider,
                                           @Nullable List<ReaderConfig> alternativeConfig) {
        this.db = dbProvider.bySchema(CORE_AGENT);
        this.alternativeConfig = alternativeConfig != null ? alternativeConfig : List.of();
    }

    public List<ReaderConfig> readConfig() {
        List<ReaderConfig> dbConfig = readConfigFromDatabase();
        return !dbConfig.isEmpty() ? dbConfig : alternativeConfig;
    }

    public void writeConfig(List<ReaderConfig> readers) {
        db.execute(sql -> {
            List<ReaderRecord> readerRecords = readers.stream()
                    .map(r -> new ReaderRecord(r.readerSn, r.endpoint.toString()))
                    .collect(toList());
            List<AntennaRecord> antennaRecords = readers.stream()
                    .flatMap(r -> stream(r.enabledAntennae).mapToObj(portNum -> new AntennaRecord(r.readerSn, portNum)))
                    .collect(toList());

            sql.truncate(READER).cascade().execute();

            sql.batchInsert(readerRecords).execute();
            sql.batchInsert(antennaRecords).execute();
            return null;
        });
    }

    private List<ReaderConfig> readConfigFromDatabase() {
        return db.execute(sql -> {
            List<ReaderRecord> readers = sql.selectFrom(READER).fetch();
            Map<String, List<Integer>> antennae = sql.selectFrom(ANTENNA).stream().collect(
                    groupingBy(AntennaRecord::getReaderSn, mapping(AntennaRecord::getPortNumber, toList())));
            return readers.stream()
                    .map(r -> new ReaderConfig(r.getReaderSn(), HostAndPort.fromString(r.getEndpoint()),
                            antennae.get(r.getReaderSn()).stream().mapToInt(i -> i).toArray()))
                    .collect(toList());
        });
    }
}
