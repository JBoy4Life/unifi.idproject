package id.unifi.service.core.processing;

import com.coreoz.wisp.schedule.FixedHourSchedule;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import id.unifi.service.core.CoreService;
import org.jooq.Record1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.ZoneId;
import java.util.List;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.*;
import com.coreoz.wisp.Scheduler;

public class VisitScheduler {

    private static final Logger log = LoggerFactory.getLogger(CoreService.class);
    private static final Scheduler scheduler = new Scheduler();
    private final Database db;
    private final ProcessVisit visitProcessor;


    public VisitScheduler (DatabaseProvider dbProvider , ProcessVisit visitProcessor) {
        this.db = dbProvider.bySchema(CORE);
        this.visitProcessor = visitProcessor;

    }

    public void visitSchedule() {
        log.info("INITIALIZING VISIT SCHEDULER");


        List<String> clientZoneIds = db.execute(sql ->
                sql.selectDistinct(SITE.TIME_ZONE).from(SITE).fetch(Record1::value1));

        for (var clientZone : clientZoneIds) {
            scheduler.schedule(
                    () -> visitProcessor.insertVisits(clientZone),
                    new FixedHourSchedule("05:00", ZoneId.of(clientZone))
            );

        }

    }

}








