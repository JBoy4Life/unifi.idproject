package id.unifi.service.attendance.services;

import id.unifi.service.attendance.db.Keys;
import static id.unifi.service.attendance.db.Tables.ASSIGNMENT;
import static id.unifi.service.attendance.db.Tables.BLOCK;
import static id.unifi.service.attendance.db.Tables.SCHEDULE;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import static id.unifi.service.common.db.DatabaseProvider.CORE_SCHEMA_NAME;
import id.unifi.service.common.operator.OperatorPK;
import id.unifi.service.common.operator.OperatorSessionData;
import static id.unifi.service.core.db.Tables.CONTACT;
import static java.util.stream.Collectors.toMap;
import org.jooq.Record2;
import static org.jooq.impl.DSL.count;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@ApiService("schedule")
public class ScheduleService {
    private final Database db;

    public ScheduleService(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchemaName(CORE_SCHEMA_NAME);
    }

    @ApiOperation
    public List<ScheduleInfo> listSchedules(OperatorSessionData session, String clientId) {
        authorize(session, clientId);
        return db.execute(sql -> sql.selectFrom(SCHEDULE)
                .where(SCHEDULE.CLIENT_ID.eq(clientId))
                .fetch(r -> new ScheduleInfo(r.getScheduleId(), r.getName())));
    }

    @ApiOperation
    public List<ScheduleStat> listScheduleStats(OperatorSessionData session, String clientId) {
        authorize(session, clientId);
        Instant runsFrom = Instant.parse("2017-10-08T12:00:00Z");
        Instant runsTo = Instant.parse("2018-06-21T12:00:00Z");
        return db.execute(sql -> {
            Map<String, Integer> blockCount = sql.select(SCHEDULE.SCHEDULE_ID, count(BLOCK.BLOCK_ID))
                    .from(SCHEDULE).leftJoin(BLOCK).onKey()
                    .where(SCHEDULE.CLIENT_ID.eq(clientId))
                    .groupBy(Keys.SCHEDULE_PKEY.getFieldsArray())
                    .stream()
                    .collect(toMap(r -> r.get(SCHEDULE.SCHEDULE_ID), Record2::value2));
            return sql.select(SCHEDULE.SCHEDULE_ID, count(ASSIGNMENT.CLIENT_REFERENCE))
                    .from(SCHEDULE).leftJoin(ASSIGNMENT).onKey()
                    .where(SCHEDULE.CLIENT_ID.eq(clientId))
                    .groupBy(Keys.SCHEDULE_PKEY.getFieldsArray())
                    .fetch(r -> new ScheduleStat(
                            r.get(SCHEDULE.SCHEDULE_ID),
                            runsFrom,
                            runsTo,
                            r.value2(),
                            blockCount.get(r.get(ASSIGNMENT.SCHEDULE_ID)),
                            randomAttendance()));
        });
    }

    private static BigDecimal randomAttendance() {
        return BigDecimal.valueOf(new Random().nextInt(1001), 1);
    }

    private static OperatorPK authorize(OperatorSessionData sessionData, String clientId) {
        return Optional.ofNullable(sessionData.getOperator())
                .filter(op -> op.clientId.equals(clientId))
                .orElseThrow(Unauthorized::new);
    }

    public static class ScheduleInfo {
        public final String scheduleId;
        public final String name;

        public ScheduleInfo(String scheduleId, String name) {
            this.scheduleId = scheduleId;
            this.name = name;
        }
    }

    public static class ScheduleStat {
        public final String scheduleId;
        public final Instant runsFrom;
        public final Instant runsTo;
        public final int attendeeCount;
        public final int blockCount;
        public final BigDecimal overallAttendance;

        public <T> ScheduleStat(String scheduleId,
                                Instant runsFrom,
                                Instant runsTo,
                                int attendeeCount,
                                int blockCount,
                                BigDecimal overallAttendance) {
            this.scheduleId = scheduleId;
            this.runsFrom = runsFrom;
            this.runsTo = runsTo;
            this.attendeeCount = attendeeCount;
            this.blockCount = blockCount;
            this.overallAttendance = overallAttendance;
        }
    }
}
