package id.unifi.service.attendance.services;

import id.unifi.service.attendance.AttendanceMatcher;
import id.unifi.service.attendance.OverriddenStatus;
import static id.unifi.service.attendance.OverriddenStatus.ABSENT;
import static id.unifi.service.attendance.OverriddenStatus.AUTH_ABSENT;
import static id.unifi.service.attendance.OverriddenStatus.PRESENT;
import static id.unifi.service.attendance.db.Attendance.ATTENDANCE;
import id.unifi.service.attendance.db.Keys;
import static id.unifi.service.attendance.db.Tables.*;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.types.pk.OperatorPK;
import static id.unifi.service.common.util.TimeUtils.zonedFromUtcLocal;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.ANTENNA;
import static id.unifi.service.core.db.Tables.HOLDER;
import static id.unifi.service.core.db.Tables.HOLDER_METADATA;
import static id.unifi.service.core.db.Tables.ZONE;
import id.unifi.service.core.db.tables.records.HolderRecord;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import static java.time.ZoneOffset.UTC;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import org.jooq.*;
import org.jooq.impl.DSL;
import static org.jooq.impl.DSL.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApiService("schedule")
public class ScheduleService {
    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);

    private static final Field<String> CLIENT_ID = unqualified(ATTENDANCE_.CLIENT_ID);
    private static final Field<String> CLIENT_REFERENCE = unqualified(ATTENDANCE_.CLIENT_REFERENCE);
    private static final Field<String> SCHEDULE_ID = unqualified(ATTENDANCE_.SCHEDULE_ID);
    private static final Field<String> BLOCK_ID = unqualified(ATTENDANCE_.BLOCK_ID);
    private static final Field<String> SITE_ID = unqualified(ZONE.SITE_ID);
    private static final Field<String> ZONE_ID = unqualified(ZONE.ZONE_ID);
    private static final Field<String> OVERRIDDEN_STATUS = field(name("overridden_status"), String.class);
    private static final Field<LocalDateTime> EPOCH = value(LocalDateTime.ofInstant(Instant.EPOCH, UTC));

    private static final Table<Record> BLOCK_WITH_TIME_AND_ZONE =
            BLOCK.join(BLOCK_TIME).using(CLIENT_ID, SCHEDULE_ID, BLOCK_ID)
                    .join(BLOCK_ZONE).using(CLIENT_ID, SCHEDULE_ID, BLOCK_ID);

    private static final CommonTableExpression<Record5<String, String, String, String, String>> FULL_ATTENDANCE =
                name("fa").as(select(CLIENT_ID, CLIENT_REFERENCE, SCHEDULE_ID, BLOCK_ID, ATTENDANCE_OVERRIDE.STATUS.as(OVERRIDDEN_STATUS))
                        .distinctOn(CLIENT_ID, CLIENT_REFERENCE, SCHEDULE_ID, BLOCK_ID)
                        .from(ATTENDANCE_)
                        .fullJoin(ATTENDANCE_OVERRIDE)
                        .using(CLIENT_ID, CLIENT_REFERENCE, SCHEDULE_ID, BLOCK_ID));

    private static final CommonTableExpression<Record4<String, String, String, OffsetDateTime>> ZONE_PROCESSING_STATE =
            name("z").as(select(ZONE.CLIENT_ID, ZONE.SITE_ID, ZONE.ZONE_ID,
                    min(coalesce(PROCESSING_STATE.PROCESSED_UP_TO, EPOCH)).as("processed_up_to"))
                    .from((ZONE.leftJoin(ANTENNA).onKey())
                    .leftJoin(PROCESSING_STATE).on(
                            ANTENNA.CLIENT_ID.eq(PROCESSING_STATE.CLIENT_ID),
                            ANTENNA.READER_SN.eq(PROCESSING_STATE.READER_SN),
                            ANTENNA.PORT_NUMBER.eq(PROCESSING_STATE.PORT_NUMBER)))
                    .groupBy(ZONE.CLIENT_ID, ZONE.SITE_ID, ZONE.ZONE_ID));

    private static final Field<LocalDateTime> ZONE_PROCESSED_UP_TO = ZONE_PROCESSING_STATE.field("processed_up_to", LocalDateTime.class);
    private static final Field<LocalDateTime> BLOCK_DETECTION_END_TIME = field("{0} + {1} * interval '1 second'",
            LocalDateTime.class, BLOCK_TIME.END_TIME, AttendanceMatcher.DETECTION_AFTER_BLOCK_END.toSeconds());
    private static final Condition ZONE_PROCESSED = ZONE_PROCESSED_UP_TO.ge(BLOCK_DETECTION_END_TIME);

    private static final Field<String> STATUS = coalesce(
            FULL_ATTENDANCE.field(OVERRIDDEN_STATUS),
            when(FULL_ATTENDANCE.field(BLOCK_ID).isNotNull(), "present").when(ZONE_PROCESSED, "absent")
    ).as("status");

    private static final Field<Boolean> STATUS_OVERRIDDEN = field(FULL_ATTENDANCE.field(OVERRIDDEN_STATUS).isNotNull());

    private static final Condition IS_PRESENT_OR_AUTH_ABSENT = condition(coalesce(
            field(FULL_ATTENDANCE.field(OVERRIDDEN_STATUS).in(PRESENT.toString(), AUTH_ABSENT.toString())),
            field(FULL_ATTENDANCE.field(BLOCK_ID).isNotNull())
    ));
    private static final Condition IS_ABSENT = condition(coalesce(
            field(FULL_ATTENDANCE.field(OVERRIDDEN_STATUS).eq(ABSENT.toString())),
            field(ZONE_PROCESSED.and(FULL_ATTENDANCE.field(BLOCK_ID).isNull()))
    ));
    private static final Field<BigDecimal> ATTENDANCE_RATIO =
            field("{0}::decimal / {1}", BigDecimal.class, count().filterWhere(IS_PRESENT_OR_AUTH_ABSENT), count());

    private final Database db;

    public ScheduleService(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE, ATTENDANCE);
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
        return db.execute(sql -> sql.selectFrom(BLOCK_WITH_TIME_AND_ZONE)
                .where(BLOCK.CLIENT_ID.eq(clientId))
                .and(BLOCK.SCHEDULE_ID.eq(scheduleId))
                .fetch(r -> new BlockInfo(
                        r.get(BLOCK.BLOCK_ID),
                        r.get(BLOCK.NAME),
                        zonedFromUtcLocal(r.get(BLOCK_TIME.START_TIME)),
                        zonedFromUtcLocal(r.get(BLOCK_TIME.END_TIME)),
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
        var stats = db.execute(sql -> fetchScheduleStats(
                sql, clientId, SCHEDULE.SCHEDULE_ID.eq(scheduleId), SCHEDULE_ID.eq(scheduleId)));
        return stats.stream().findFirst().orElse(null);
    }

    @ApiOperation
    public ContactAttendanceInfo getContactAttendanceForSchedule(OperatorSessionData session,
                                                                 String clientId,
                                                                 String scheduleId) {
        authorize(session, clientId);
        return db.execute(sql -> {
            var blockCount = sql.fetchCount(BLOCK, BLOCK.CLIENT_ID.eq(clientId).and(BLOCK.SCHEDULE_ID.eq(scheduleId)));

            var names = sql.select(ASSIGNMENT.CLIENT_REFERENCE, HOLDER.NAME)
                    .from(ASSIGNMENT)
                    .leftJoin(HOLDER)
                    .on(ASSIGNMENT.CLIENT_ID.eq(HOLDER.CLIENT_ID), ASSIGNMENT.CLIENT_REFERENCE.eq(HOLDER.CLIENT_REFERENCE))
                    .where(ASSIGNMENT.CLIENT_ID.eq(clientId))
                    .and(ASSIGNMENT.SCHEDULE_ID.eq(scheduleId))
                    .stream()
                    .collect(toMap(Record2::value1, Record2::value2));

            var attendance = sql
                    .with(FULL_ATTENDANCE, ZONE_PROCESSING_STATE)
                    .select(ASSIGNMENT.CLIENT_REFERENCE,
                            count().filterWhere(IS_PRESENT_OR_AUTH_ABSENT),
                            count().filterWhere(IS_ABSENT))
                    .from(ASSIGNMENT)
                    .join(BLOCK_WITH_TIME_AND_ZONE)
                    .using(CLIENT_ID, SCHEDULE_ID)
                    .join(ZONE_PROCESSING_STATE)
                    .using(CLIENT_ID, SITE_ID, ZONE_ID)
                    .leftJoin(FULL_ATTENDANCE)
                    .using(CLIENT_ID, CLIENT_REFERENCE, SCHEDULE_ID, BLOCK_ID)
                    .where(ASSIGNMENT.CLIENT_ID.eq(clientId))
                    .and(ASSIGNMENT.SCHEDULE_ID.eq(scheduleId))
                    .and(ZONE_PROCESSED)
                    .groupBy(ASSIGNMENT.CLIENT_ID, ASSIGNMENT.CLIENT_REFERENCE, ASSIGNMENT.SCHEDULE_ID)
                    .fetch(r -> new ContactAttendance(r.value1(), names.get(r.value1()), r.value2(), r.value3()));
            return new ContactAttendanceInfo(blockCount, attendance);
        });
    }

    @ApiOperation
    public void putAssignment(OperatorSessionData session,
                              String clientId,
                              String clientReference,
                              String scheduleId) {
        var operator = authorize(session, clientId);

        db.execute(sql -> sql.insertInto(ASSIGNMENT)
                .set(ASSIGNMENT.CLIENT_ID, operator.clientId)
                .set(ASSIGNMENT.CLIENT_REFERENCE, clientReference)
                .set(ASSIGNMENT.SCHEDULE_ID, scheduleId)
                .onConflictDoNothing()
                .execute());
    }

    @ApiOperation
    public List<BlockAttendance> reportBlockAttendance(OperatorSessionData session,
                                                       String clientId,
                                                       String clientReference,
                                                       String scheduleId) {
        authorize(session, clientId);
        return db.execute(sql -> {
            var q = sql
                    .with(FULL_ATTENDANCE, ZONE_PROCESSING_STATE)
                    .select(BLOCK_ID,
                            BLOCK.NAME,
                            BLOCK_TIME.START_TIME,
                            BLOCK_TIME.END_TIME,
                            BLOCK_ZONE.SITE_ID,
                            BLOCK_ZONE.ZONE_ID,
                            STATUS_OVERRIDDEN,
                            STATUS)
                    .from(ASSIGNMENT)
                    .join(BLOCK_WITH_TIME_AND_ZONE)
                    .using(CLIENT_ID, SCHEDULE_ID)
                    .join(ZONE_PROCESSING_STATE)
                    .using(CLIENT_ID, SITE_ID, ZONE_ID)
                    .leftJoin(FULL_ATTENDANCE)
                    .using(CLIENT_ID, CLIENT_REFERENCE, SCHEDULE_ID, BLOCK_ID)
                    .where(ASSIGNMENT.CLIENT_ID.eq(clientId))
                    .and(ASSIGNMENT.SCHEDULE_ID.eq(scheduleId))
                    .and(ASSIGNMENT.CLIENT_REFERENCE.eq(clientReference));
                    return q
                            .fetch(r -> new BlockAttendance(
                                    scheduleId,
                                    r.get(BLOCK_ID),
                                    r.get(BLOCK.NAME),
                                    zonedFromUtcLocal(r.get(BLOCK_TIME.START_TIME)),
                                    zonedFromUtcLocal(r.get(BLOCK_TIME.END_TIME)),
                                    r.get(BLOCK_ZONE.SITE_ID),
                                    r.get(BLOCK_ZONE.ZONE_ID),
                                    OverriddenStatus.fromString(r.get(STATUS)),
                                    r.get(STATUS_OVERRIDDEN)));
                }
        );
    }

    @ApiOperation
    public ContactScheduleAttendanceInfo reportContactScheduleAttendance(OperatorSessionData session, String clientId) {
        authorize(session, clientId);

        return db.execute(sql -> {
            var schedules = sql
                    .select(SCHEDULE.SCHEDULE_ID, SCHEDULE.NAME, count())
                    .from(SCHEDULE.leftJoin(BLOCK).onKey())
                    .where(SCHEDULE.CLIENT_ID.eq(clientId))
                    .groupBy(SCHEDULE.SCHEDULE_ID, SCHEDULE.NAME)
                    .fetch(r -> new ScheduleInfoWithBlockCount(r.get(SCHEDULE.SCHEDULE_ID), r.get(SCHEDULE.NAME), r.value3()));

            var contactNames = sql
                    .selectFrom(HOLDER)
                    .where(HOLDER.CLIENT_ID.eq(clientId))
                    .and(HOLDER.HOLDER_TYPE.eq("contact"))
                    .stream()
                    .collect(toMap(HolderRecord::getClientReference, HolderRecord::getName));

            var attendance = sql
                    .with(FULL_ATTENDANCE, ZONE_PROCESSING_STATE)
                    .select(ASSIGNMENT.CLIENT_REFERENCE,
                            ASSIGNMENT.SCHEDULE_ID,
                            count().filterWhere(IS_PRESENT_OR_AUTH_ABSENT),
                            count().filterWhere(IS_ABSENT))
                    .from(ASSIGNMENT)
                    .join(BLOCK_WITH_TIME_AND_ZONE)
                    .using(CLIENT_ID, SCHEDULE_ID)
                    .join(ZONE_PROCESSING_STATE)
                    .using(CLIENT_ID, SITE_ID, ZONE_ID)
                    .leftJoin(FULL_ATTENDANCE)
                    .using(CLIENT_ID, CLIENT_REFERENCE, SCHEDULE_ID, BLOCK_ID)
                    .where(CLIENT_ID.eq(clientId))
                    .and(ZONE_PROCESSED)
                    .groupBy(CLIENT_REFERENCE, SCHEDULE_ID)
                    .stream()
                    .collect(groupingBy(r -> r.get(ASSIGNMENT.CLIENT_REFERENCE),
                            mapping(r -> new ScheduleAttendance(r.value2(), r.value3(), r.value4()), toList())))
                    .entrySet().stream()
                    .map(e -> new ContactScheduleAttendance(e.getKey(), contactNames.get(e.getKey()), e.getValue()))
                    .collect(toList());

            return new ContactScheduleAttendanceInfo(schedules, attendance);
        });
    }

    @ApiOperation
    public LowAttendanceReport reportLowAttendanceByMetadata(OperatorSessionData session,
                                                             String clientId,
                                                             String metadataKey,
                                                             String metadataValue,
                                                             BigDecimal attendanceThreshold,
                                                             @Nullable OffsetDateTime startTime,
                                                             @Nullable OffsetDateTime endTime) {
        authorize(session, clientId);
        return db.execute(sql -> {
            var attendance = sql
                    .with(FULL_ATTENDANCE, ZONE_PROCESSING_STATE)
                    .select(ASSIGNMENT.CLIENT_REFERENCE,
                            ASSIGNMENT.SCHEDULE_ID,
                            count().filterWhere(IS_PRESENT_OR_AUTH_ABSENT),
                            count().filterWhere(IS_ABSENT))
                    .from(HOLDER.join(HOLDER_METADATA).using(CLIENT_ID, CLIENT_REFERENCE))
                    .join(ASSIGNMENT)
                    .using(CLIENT_ID, CLIENT_REFERENCE)
                    .join(BLOCK_WITH_TIME_AND_ZONE)
                    .using(CLIENT_ID, SCHEDULE_ID)
                    .join(ZONE_PROCESSING_STATE)
                    .using(CLIENT_ID, SITE_ID, ZONE_ID)
                    .leftJoin(FULL_ATTENDANCE)
                    .using(CLIENT_ID, CLIENT_REFERENCE, SCHEDULE_ID, BLOCK_ID)
                    .where(ASSIGNMENT.CLIENT_ID.eq(clientId))
                    .and(between(startTime, endTime))
                    .and(metadataKeyEquals(metadataKey, metadataValue))
                    .and(ZONE_PROCESSED)
                    .groupBy(ASSIGNMENT.CLIENT_ID, ASSIGNMENT.CLIENT_REFERENCE, ASSIGNMENT.SCHEDULE_ID)
                    .having(ATTENDANCE_RATIO.lt(attendanceThreshold))
                    .fetch(r -> new ContactScheduleSummaryAttendance(r.value1(), r.value2(), r.value3(), r.value4()));

            // Get the start time of the first block if there is one
            ZonedDateTime actualStartTime = null;
            if (startTime == null) {
                var scheduleIds = attendance.stream().map(a -> a.scheduleId).collect(Collectors.toSet());
                actualStartTime = zonedFromUtcLocal(sql.select(min(BLOCK_TIME.START_TIME))
                        .from(BLOCK_TIME)
                        .where(BLOCK_TIME.SCHEDULE_ID.in(scheduleIds))
                        .fetchOne(Record1::value1));
            }
            return new LowAttendanceReport(actualStartTime, attendance);
        });
    }

    @ApiOperation
    public void overrideAttendance(OperatorSessionData session,
                                   String clientId,
                                   String clientReference,
                                   String scheduleId,
                                   String blockId,
                                   OverriddenStatus status) {
        var operator = authorize(session, clientId);
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

    private static Condition between(@Nullable OffsetDateTime startTime, @Nullable OffsetDateTime endTime) {
        var startCond = startTime != null ? BLOCK_TIME.START_TIME.greaterOrEqual(utcLocalFromOffset(startTime)) : null;
        var endCond = endTime != null ? BLOCK_TIME.START_TIME.lessOrEqual(utcLocalFromOffset(endTime)) : null;
        return DSL.and(Stream.of(startCond, endCond).filter(Objects::nonNull).toArray(Condition[]::new));
    }

    private static LocalDateTime utcLocalFromOffset(OffsetDateTime date) {
        return LocalDateTime.ofInstant(date.toInstant(), UTC);
    }

    private static Condition metadataKeyEquals(String key, String value) {
        return field("{0} ->> {1}", String.class, HOLDER_METADATA.METADATA, value(key)).eq(value);
    }

    private static List<ScheduleStat> fetchScheduleStats(DSLContext sql,
                                                         String clientId,
                                                         Condition scheduleCondition,
                                                         Condition condition) {
        var blockSummary = sql
                .with(ZONE_PROCESSING_STATE)
                .select(SCHEDULE.SCHEDULE_ID,
                        count(BLOCK.BLOCK_ID),
                        min(BLOCK_TIME.START_TIME),
                        max(BLOCK_TIME.END_TIME),
                        count().filterWhere(ZONE_PROCESSED))
                .from(SCHEDULE)
                .leftJoin(BLOCK_WITH_TIME_AND_ZONE).using(CLIENT_ID, SCHEDULE_ID)
                .leftJoin(ZONE_PROCESSING_STATE)
                .using(CLIENT_ID, SITE_ID, ZONE_ID)
                .where(CLIENT_ID.eq(clientId))
                .and(scheduleCondition)
                .groupBy(Keys.SCHEDULE_PKEY.getFieldsArray())
                .stream()
                .collect(toMap(r -> r.get(SCHEDULE.SCHEDULE_ID), identity()));
        var scheduleAttendance = sql
                .with(FULL_ATTENDANCE, ZONE_PROCESSING_STATE)
                .select(SCHEDULE_ID, count())
                .from(FULL_ATTENDANCE)
                .join(BLOCK_WITH_TIME_AND_ZONE)
                .using(CLIENT_ID, SCHEDULE_ID, BLOCK_ID)
                .join(ZONE_PROCESSING_STATE)
                .using(CLIENT_ID, SITE_ID, ZONE_ID)
                .where(FULL_ATTENDANCE.field(CLIENT_ID).eq(clientId))
                .and(condition)
                .and(ZONE_PROCESSED) // so that overall attendance doesn't go over 100%; TODO: elaborate
                .and(IS_PRESENT_OR_AUTH_ABSENT)
                .groupBy(SCHEDULE_ID)
                .stream()
                .collect(toMap(r -> r.get(SCHEDULE_ID), Record2::value2));
        return sql
                .select(SCHEDULE.SCHEDULE_ID, SCHEDULE.NAME, count(ASSIGNMENT.CLIENT_REFERENCE))
                .from(SCHEDULE)
                .leftJoin(ASSIGNMENT).onKey(Keys.ASSIGNMENT__FK_ASSIGNMENT_TO_SCHEDULE)
                .where(SCHEDULE.CLIENT_ID.eq(clientId))
                .and(scheduleCondition)
                .groupBy(Keys.SCHEDULE_PKEY.getFieldsArray())
                .fetch(r -> {
                    var scheduleId = r.get(SCHEDULE.SCHEDULE_ID);
                    var scheduleName = r.get(SCHEDULE.NAME);
                    var stats = blockSummary.get(scheduleId);
                    return new ScheduleStat(
                            scheduleId,
                            scheduleName,
                            zonedFromUtcLocal(stats.value3()),
                            zonedFromUtcLocal(stats.value4()),
                            r.value3(),
                            stats.value2(),
                            Optional.ofNullable(scheduleAttendance.get(r.get(ASSIGNMENT.SCHEDULE_ID))).orElse(0),
                            stats.value5());
                });
    }

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
        public final ZonedDateTime startTime;
        public final ZonedDateTime endTime;
        public final int committerCount;
        public final int blockCount;
        public final int overallAttendance;
        public final int processedBlockCount;

        public ScheduleStat(String scheduleId,
                            String name,
                            ZonedDateTime startTime,
                            ZonedDateTime endTime,
                            int committerCount,
                            int blockCount,
                            int overallAttendance,
                            int processedBlockCount) {
            this.scheduleId = scheduleId;
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
            this.committerCount = committerCount;
            this.blockCount = blockCount;
            this.processedBlockCount = processedBlockCount;
            this.overallAttendance = overallAttendance;
        }
    }

    public static class BlockInfo {
        public final String blockId;
        public final String name;
        public final ZonedDateTime startTime;
        public final ZonedDateTime endTime;
        public final String siteId;
        public final String zoneId;

        public BlockInfo(String blockId,
                         String name,
                         ZonedDateTime startTime,
                         ZonedDateTime endTime,
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

    public static class ContactAttendance {
        public final String clientReference;
        public final String name;
        public final int presentCount;
        public final int absentCount;

        public ContactAttendance(String clientReference, String name, int presentCount, int absentCount) {
            this.clientReference = clientReference;
            this.name = name;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
        }
    }

    public static class ContactAttendanceInfo {
        public final int blockCount;
        public final List<ContactAttendance> attendance;

        public ContactAttendanceInfo(int blockCount, List<ContactAttendance> attendance) {
            this.blockCount = blockCount;
            this.attendance = attendance;
        }
    }

    public static class BlockAttendance {
        public final String scheduleId;
        public final String blockId;
        public final String name;
        public final ZonedDateTime startTime;
        public final ZonedDateTime endTime;
        public final String siteId;
        public final String zoneId;
        public final OverriddenStatus status;
        public final boolean statusOverridden;

        public BlockAttendance(String scheduleId,
                               String blockId,
                               String name,
                               ZonedDateTime startTime,
                               ZonedDateTime endTime,
                               String siteId,
                               String zoneId,
                               OverriddenStatus status,
                               boolean statusOverridden) {
            this.scheduleId = scheduleId;
            this.blockId = blockId;
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
            this.siteId = siteId;
            this.zoneId = zoneId;
            this.status = status;
            this.statusOverridden = statusOverridden;
        }
    }

    public static class ScheduleAttendance {
        public final String scheduleId;
        public final int presentCount;
        public final int absentCount;

        public ScheduleAttendance(String scheduleId, int presentCount, int absentCount) {
            this.scheduleId = scheduleId;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
        }
    }

    public static class ContactScheduleAttendance {
        public final String clientReference;
        public final String name;
        public final List<ScheduleAttendance> attendance;

        public ContactScheduleAttendance(String clientReference, String name, List<ScheduleAttendance> attendance) {
            this.clientReference = clientReference;
            this.name = name;
            this.attendance = attendance;
        }
    }

    public static class ContactScheduleAttendanceInfo {
        public final List<ScheduleInfoWithBlockCount> schedules;
        public final List<ContactScheduleAttendance> attendance;

        public ContactScheduleAttendanceInfo(List<ScheduleInfoWithBlockCount> schedules, List<ContactScheduleAttendance> attendance) {
            this.schedules = schedules;
            this.attendance = attendance;
        }
    }

    public static class ScheduleInfoWithBlockCount {
        public final String scheduleId;
        public final String name;
        public final int blockCount;

        public ScheduleInfoWithBlockCount(String scheduleId, String name, int blockCount) {
            this.scheduleId = scheduleId;
            this.name = name;
            this.blockCount = blockCount;
        }
    }

    public static class ContactScheduleSummaryAttendance {
        public final String clientReference;
        public final String scheduleId;
        public final int presentCount;
        public final int absentCount;

        public ContactScheduleSummaryAttendance(String clientReference, String scheduleId, int presentCount, int absentCount) {
            this.clientReference = clientReference;
            this.scheduleId = scheduleId;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
        }
    }

    public static class LowAttendanceReport {
        public final ZonedDateTime startTime;
        public final List<ContactScheduleSummaryAttendance> attendance;

        public LowAttendanceReport(@Nullable ZonedDateTime startTime,
                                   List<ContactScheduleSummaryAttendance> attendance) {
            this.startTime = startTime;
            this.attendance = attendance;
        }
    }
}
