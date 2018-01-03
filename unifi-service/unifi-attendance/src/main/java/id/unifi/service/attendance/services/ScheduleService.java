package id.unifi.service.attendance.services;

import id.unifi.service.attendance.OverriddenStatus;
import id.unifi.service.attendance.db.Keys;
import id.unifi.service.attendance.db.Tables;
import static id.unifi.service.attendance.db.Tables.*;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import static id.unifi.service.common.db.DatabaseProvider.CORE_SCHEMA_NAME;
import id.unifi.service.common.operator.OperatorPK;
import id.unifi.service.common.operator.OperatorSessionData;
import static id.unifi.service.common.util.TimeUtils.instantFromUtcLocal;
import static id.unifi.service.core.db.Tables.HOLDER;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.Record4;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import static org.jooq.impl.DSL.*;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApiService("schedule")
public class ScheduleService {
    private static final Field<String> CLIENT_REFERENCE = unqualified(ATTENDANCE_.CLIENT_REFERENCE);
    private static final Field<String> SCHEDULE_ID = unqualified(ATTENDANCE_.SCHEDULE_ID);
    private static final Field<String> BLOCK_ID = unqualified(ATTENDANCE_.BLOCK_ID);
    private static final Field<String> CLIENT_ID = unqualified(ATTENDANCE_.CLIENT_ID);

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
    public List<BlockInfo> listBlocks(OperatorSessionData session, String clientId, String scheduleId) {
        authorize(session, clientId);
        return db.execute(sql -> sql.selectFrom(BLOCK
                .leftJoin(Tables.BLOCK_TIME).onKey(Keys.BLOCK_TIME__FK_BLOCK_TIME_TO_BLOCK)
                .leftJoin(Tables.BLOCK_ZONE).onKey(Keys.BLOCK_ZONE__FK_BLOCK_ZONE_TO_BLOCK))
                .where(BLOCK.CLIENT_ID.eq(clientId))
                .and(BLOCK.SCHEDULE_ID.eq(scheduleId))
                .fetch(r -> new BlockInfo(
                        r.get(BLOCK.BLOCK_ID),
                        r.get(BLOCK.NAME),
                        instantFromUtcLocal(r.get(BLOCK_TIME.START_TIME)),
                        instantFromUtcLocal(r.get(BLOCK_TIME.END_TIME)),
                        r.get(BLOCK_ZONE.SITE_ID),
                        r.get(BLOCK_ZONE.ZONE_ID))));
    }

    @ApiOperation
    public List<ScheduleStat> listScheduleStats(OperatorSessionData session, String clientId) {
        authorize(session, clientId);
        return db.execute(sql -> fetchScheduleStats(sql, clientId, trueCondition(), trueCondition()));
    }

    @ApiOperation
    public @Nullable ScheduleStat getSchedule(OperatorSessionData session, String clientId, String scheduleId) {
        authorize(session, clientId);
        List<ScheduleStat> stats = db.execute(sql -> fetchScheduleStats(
                sql, clientId, SCHEDULE.SCHEDULE_ID.eq(scheduleId), field(name("schedule_id")).eq(scheduleId)));
        return stats.stream().findFirst().orElse(null);
    }

    @ApiOperation
    public ContactAttendanceInfo getContactAttendanceForSchedule(OperatorSessionData session,
                                                                 String clientId,
                                                                 String scheduleId) {
        authorize(session, clientId);
        return db.execute(sql -> {
            int blockCount = sql.fetchCount(BLOCK, BLOCK.CLIENT_ID.eq(clientId).and(BLOCK.SCHEDULE_ID.eq(scheduleId)));

            Map<String, String> names = sql.select(ASSIGNMENT.CLIENT_REFERENCE, HOLDER.NAME)
                    .from(ASSIGNMENT)
                    .leftJoin(HOLDER)
                    .on(ASSIGNMENT.CLIENT_ID.eq(HOLDER.CLIENT_ID), ASSIGNMENT.CLIENT_REFERENCE.eq(HOLDER.CLIENT_REFERENCE))
                    .where(ASSIGNMENT.CLIENT_ID.eq(clientId))
                    .and(ASSIGNMENT.SCHEDULE_ID.eq(scheduleId))
                    .stream()
                    .collect(toMap(Record2::value1, Record2::value2));

            List<ContactAttendance> attendance =
                    sql.select(ASSIGNMENT.CLIENT_REFERENCE, count(ATTENDANCE_.CLIENT_REFERENCE))
                            .from(ASSIGNMENT.leftJoin(ATTENDANCE_).onKey())
                            .where(ASSIGNMENT.CLIENT_ID.eq(clientId))
                            .and(ASSIGNMENT.SCHEDULE_ID.eq(scheduleId))
                            .groupBy(Keys.ASSIGNMENT_PKEY.getFieldsArray())
                            .fetch(r -> new ContactAttendance(r.value1(), names.get(r.value1()), r.value2()));
            return new ContactAttendanceInfo(blockCount, attendance);
        });
    }

    @ApiOperation
    public List<BlockAttendance> reportBlockAttendance(OperatorSessionData session,
                                                       String clientId,
                                                       String clientReference,
                                                       String scheduleId) {
        OperatorPK operator = authorize(session, clientId);
        return db.execute(sql -> sql.select(BLOCK.BLOCK_ID, BLOCK.NAME, BLOCK_TIME.START_TIME, BLOCK_TIME.END_TIME, field(field(name("thing", "block_id")).isNotNull()), field(name("thing", "status"), String.class))
                    .from(BLOCK.leftJoin(BLOCK_TIME).onKey())
                    .join(ASSIGNMENT).on(BLOCK.CLIENT_ID.eq(ASSIGNMENT.CLIENT_ID), BLOCK.SCHEDULE_ID.eq(ASSIGNMENT.SCHEDULE_ID))
                    .leftJoin(table("(select distinct on (client_id, client_reference, schedule_id, block_id) client_id, client_reference, schedule_id, block_id, attendance_override.status from attendance.attendance full join attendance.attendance_override using (client_id, client_reference, schedule_id, block_id) order by client_id, client_reference, schedule_id, block_id, attendance_override.override_time DESC)").asTable("thing"))
                    .on(
                            ASSIGNMENT.CLIENT_ID.eq(field(name("thing", "client_id"), String.class)),
                            ASSIGNMENT.CLIENT_REFERENCE.eq(field(name("thing", "client_reference"), String.class)),
                            ASSIGNMENT.SCHEDULE_ID.eq(field(name("thing", "schedule_id"), String.class)),
                            BLOCK.BLOCK_ID.eq(field(name("thing", "block_id"), String.class)))
                    .where(ASSIGNMENT.CLIENT_ID.eq(clientId), ASSIGNMENT.SCHEDULE_ID.eq(scheduleId), ASSIGNMENT.CLIENT_REFERENCE.eq(clientReference))
                    .fetch(r -> new BlockAttendance(r.get(BLOCK_ID), r.get(BLOCK.NAME), instantFromUtcLocal(r.get(BLOCK_TIME.START_TIME)), instantFromUtcLocal(r.get(BLOCK_TIME.END_TIME)),
                            r.value6() != null ? OverriddenStatus.fromString(r.value6()) : (r.value5() ? OverriddenStatus.PRESENT : OverriddenStatus.ABSENT)))
        );
    }

    @ApiOperation
    public void overrideAttendance(OperatorSessionData session,
                                   String clientId,
                                   String clientReference,
                                   String scheduleId,
                                   String blockId,
                                   OverriddenStatus status) {
        OperatorPK operator = authorize(session, clientId);
        db.execute(sql -> {
            sql.insertInto(ATTENDANCE_OVERRIDE)
                    .set(ATTENDANCE_OVERRIDE.CLIENT_ID, operator.clientId)
                    .set(ATTENDANCE_OVERRIDE.CLIENT_REFERENCE, clientReference)
                    .set(ATTENDANCE_OVERRIDE.SCHEDULE_ID, scheduleId)
                    .set(ATTENDANCE_OVERRIDE.BLOCK_ID, blockId)
                    .set(ATTENDANCE_OVERRIDE.STATUS, status.toString())
                    .set(ATTENDANCE_OVERRIDE.OPERATOR, operator.username)
                    .execute();
            return null;
        });
    }

    private static List<ScheduleStat> fetchScheduleStats(DSLContext sql,
                                                         String clientId,
                                                         Condition scheduleCondition,
                                                         Condition condition) {
        Map<String, Record4<String, Integer, LocalDateTime, LocalDateTime>> blockSummary = sql.select(
                SCHEDULE.SCHEDULE_ID, count(BLOCK.BLOCK_ID), min(BLOCK_TIME.START_TIME), max(BLOCK_TIME.END_TIME))
                .from(SCHEDULE)
                .leftJoin(BLOCK).onKey(Keys.BLOCK__FK_BLOCK_TO_SCHEDULE)
                .leftJoin(BLOCK_TIME).onKey(Keys.BLOCK_TIME__FK_BLOCK_TIME_TO_BLOCK)
                .where(SCHEDULE.CLIENT_ID.eq(clientId))
                .and(scheduleCondition)
                .groupBy(Keys.SCHEDULE_PKEY.getFieldsArray())
                .stream()
                .collect(toMap(r -> r.get(SCHEDULE.SCHEDULE_ID), identity()));
        Map<String, Integer> scheduleAttendance =
                sql.select(SCHEDULE_ID, count())
                        .from(calculatedAttendanceTable(clientId, condition))
                        .where(field(name("status")).isDistinctFrom(OverriddenStatus.ABSENT.toString()))
                        .groupBy(SCHEDULE_ID)
                        .stream()
                        .collect(toMap(r -> r.get(SCHEDULE_ID), Record2::value2));
        return sql.select(SCHEDULE.SCHEDULE_ID, SCHEDULE.NAME, count(ASSIGNMENT.CLIENT_REFERENCE))
                .from(SCHEDULE)
                .leftJoin(ASSIGNMENT).onKey(Keys.ASSIGNMENT__FK_ASSIGNMENT_TO_SCHEDULE)
                .where(SCHEDULE.CLIENT_ID.eq(clientId))
                .and(scheduleCondition)
                .groupBy(Keys.SCHEDULE_PKEY.getFieldsArray())
                .fetch(r -> {
                    String scheduleId = r.get(SCHEDULE.SCHEDULE_ID);
                    String scheduleName = r.get(SCHEDULE.NAME);
                    Record4<String, Integer, LocalDateTime, LocalDateTime> stats = blockSummary.get(scheduleId);
                    return new ScheduleStat(
                            scheduleId,
                            scheduleName,
                            instantFromUtcLocal(stats.value3()),
                            instantFromUtcLocal(stats.value4()),
                            r.value3(),
                            stats.value2(),
                            Optional.ofNullable(scheduleAttendance.get(r.get(ASSIGNMENT.SCHEDULE_ID))).orElse(0)
                    );
                });
    }

    private static Table<Record4<String, String, String, String>> calculatedAttendanceTable(String clientId,
                                                                                            Condition condition) {
        return select(CLIENT_REFERENCE, SCHEDULE_ID, BLOCK_ID, ATTENDANCE_OVERRIDE.STATUS)
                .distinctOn(CLIENT_REFERENCE, SCHEDULE_ID, BLOCK_ID)
                .from(ATTENDANCE_)
                .fullJoin(ATTENDANCE_OVERRIDE)
                .using(CLIENT_ID, CLIENT_REFERENCE, SCHEDULE_ID, BLOCK_ID)
                .where(CLIENT_ID.eq(clientId))
                .and(condition)
                .orderBy(CLIENT_REFERENCE, SCHEDULE_ID, BLOCK_ID, ATTENDANCE_OVERRIDE.OVERRIDE_TIME.desc())
                .asTable("calculated_attendance");
    }

//    private static Table<Record4<String, String, String, String>> calculatedFullAttendanceTable(String clientId,
//                                                                                                Condition condition) {
//    }

    private static <R extends Record, T> Field<T> unqualified(TableField<R, T> field) {
        return field(name(field.getUnqualifiedName()), field.getType());
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
        public final String name;
        public final Instant startTime;
        public final Instant endTime;
        public final int committerCount;
        public final int blockCount;
        public final int overallAttendance;

        public ScheduleStat(String scheduleId,
                            String name,
                            Instant startTime,
                            Instant endTime,
                            int committerCount,
                            int blockCount,
                            int overallAttendance) {
            this.scheduleId = scheduleId;
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
            this.committerCount = committerCount;
            this.blockCount = blockCount;
            this.overallAttendance = overallAttendance;
        }
    }

    public class BlockInfo {
        public final String blockId;
        public final String name;
        public final Instant startTime;
        public final Instant endTime;
        public final String siteId;
        public final String zoneId;

        public BlockInfo(String blockId,
                         String name,
                         Instant startTime,
                         Instant endTime,
                         String siteId,
                         String zoneId) {
            this.blockId = blockId;
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
            this.siteId = siteId;
            this.zoneId = zoneId;
        }
    }

    public class ContactAttendance {
        public final String clientReference;
        public final String name;
        public final int attendedCount;

        public ContactAttendance(String clientReference, String name, int attendedCount) {
            this.clientReference = clientReference;
            this.name = name;
            this.attendedCount = attendedCount;
        }
    }

    public class ContactAttendanceInfo {
        public final int blockCount;
        public final List<ContactAttendance> attendance;

        public ContactAttendanceInfo(int blockCount, List<ContactAttendance> attendance) {
            this.blockCount = blockCount;
            this.attendance = attendance;
        }
    }

    private class BlockAttendance {
        public final String blockId;
        public final String name;
        public final Instant startTime;
        public final Instant endTime;
        public final OverriddenStatus status;

        public BlockAttendance(String blockId, String name, Instant startTime, Instant endTime, OverriddenStatus status) {
            this.blockId = blockId;
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
            this.status = status;
        }
    }
}
