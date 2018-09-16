package id.unifi.service.core.processing;

import com.coreoz.wisp.schedule.FixedHourSchedule;
import com.coreoz.wisp.Scheduler;

import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;

import org.jooq.Record1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;

import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.*;


public class VisitProcessingScheduler {
    private static final Logger log = LoggerFactory.getLogger(VisitProcessor.class);
    private static final Scheduler scheduler = new Scheduler();
    private static final String VISIT_CUTOFF_TIME = "05:00";
    private final Database db;
    private final VisitProcessor visitProcessor;

    public VisitProcessingScheduler(DatabaseProvider dbProvider, VisitProcessor visitProcessor) {
        this.db = dbProvider.bySchema(CORE, CORE);
        this.visitProcessor = visitProcessor;
    }

    public void scheduleVisitJob() {
        log.info("Initializing visit scheduler");
        var clientZoneIds = db.execute(sql ->
                sql.selectDistinct(SITE.TIME_ZONE).from(SITE).fetch(Record1::value1));

        for (var clientZone : clientZoneIds) {
            scheduler.schedule(
                    () -> visitProcessor.insertVisits(clientZone),
                    new FixedHourSchedule(VISIT_CUTOFF_TIME, ZoneId.of(clientZone))
            );
        }
    }
}
