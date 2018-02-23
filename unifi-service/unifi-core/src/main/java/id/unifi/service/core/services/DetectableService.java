package id.unifi.service.core.services;

import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.types.OperatorPK;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.ASSIGNMENT;
import static id.unifi.service.core.db.Tables.DETECTABLE;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.Table;
import static org.jooq.impl.DSL.trueCondition;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApiService("detectable")
public class DetectableService {
    private final Database db;

    public DetectableService(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
    }

    @ApiOperation
    public List<DetectableInfo> listDetectables(OperatorSessionData session,
                                                String clientId,
                                                @Nullable ListFilter filter,
                                                @Nullable Set<String> with) {
        authorize(session, clientId);
        if (filter == null) filter = ListFilter.empty();

        Table<? extends Record> tables = calculateTableJoin(filter, with);

        Condition assignedFilter = filter.assigned
                .map(assigned -> assigned
                        ? ASSIGNMENT.CLIENT_REFERENCE.isNotNull()
                        : ASSIGNMENT.CLIENT_REFERENCE.isNull())
                .orElse(trueCondition());

        Condition typeFilter = filter.detectableType
                .map(type -> DETECTABLE.DETECTABLE_TYPE.eq(type.toString()))
                .orElse(trueCondition());

        Condition activeFilter = filter.active
                .map(DETECTABLE.ACTIVE::eq)
                .orElse(trueCondition());

        return db.execute(sql -> sql
                .selectFrom(tables)
                .where(DETECTABLE.CLIENT_ID.eq(clientId))
                .and(assignedFilter)
                .and(typeFilter)
                .and(activeFilter)
                .fetch(r -> new DetectableInfo(
                        clientId,
                        r.get(DETECTABLE.DETECTABLE_ID),
                        DetectableType.fromString(r.get(DETECTABLE.DETECTABLE_TYPE)),
                        r.get(DETECTABLE.DESCRIPTION),
                        r.field(ASSIGNMENT.CLIENT_REFERENCE) == null ? null : r.get(ASSIGNMENT.CLIENT_REFERENCE))));
    }

    private static Table<? extends Record> calculateTableJoin(ListFilter filter, @Nullable Set<String> with) {
        if (with == null) with = Set.of();
        Table<? extends Record> tables = DETECTABLE;
        if (with.contains("assignment") || filter.assigned.isPresent()) {
            tables = tables.leftJoin(ASSIGNMENT).onKey();
        }
        return tables;
    }

    private static OperatorPK authorize(OperatorSessionData sessionData, String clientId) {
        return Optional.ofNullable(sessionData.getOperator())
                .filter(op -> op.clientId.equals(clientId))
                .orElseThrow(Unauthorized::new);
    }

    public static class ListFilter {
        private final Optional<Boolean> assigned;
        private final Optional<DetectableType> detectableType;
        private final Optional<Boolean> active;

        public ListFilter(Optional<Boolean> assigned,
                          Optional<DetectableType> detectableType,
                          Optional<Boolean> active) {
            this.assigned = assigned;
            this.detectableType = detectableType;
            this.active = active;
        }

        static ListFilter empty() {
            return new ListFilter(Optional.empty(), Optional.empty(), Optional.empty());
        }
    }

    public static class DetectableInfo {
        public final String clientId;
        public final String detectableId;
        public final DetectableType detectableType;
        public final String description;
        public final String assignment;

        public DetectableInfo(String clientId,
                              String detectableId,
                              DetectableType detectableType,
                              String description,
                              @Nullable String assignment) {
            this.clientId = clientId;
            this.detectableId = detectableId;
            this.detectableType = detectableType;
            this.description = description;
            this.assignment = assignment;
        }
    }
}
