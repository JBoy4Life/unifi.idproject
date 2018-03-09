package id.unifi.service.core.services;

import id.unifi.service.common.api.Validation;
import static id.unifi.service.common.api.Validation.atLeastOneNonNull;
import static id.unifi.service.common.api.Validation.shortString;
import static id.unifi.service.common.api.Validation.v;
import static id.unifi.service.common.api.Validation.validateAll;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.AlreadyExists;
import id.unifi.service.common.api.errors.NotFound;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.types.OperatorPK;
import static id.unifi.service.common.util.QueryUtils.fieldValueOpt;
import static id.unifi.service.common.util.QueryUtils.filterCondition;
import static id.unifi.service.common.util.QueryUtils.getUpdateQueryFieldMap;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.ASSIGNMENT;
import static id.unifi.service.core.db.Tables.DETECTABLE;
import id.unifi.service.core.db.tables.records.AssignmentRecord;
import id.unifi.service.core.db.tables.records.DetectableRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.InsertOnDuplicateStep;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import static org.jooq.impl.DSL.and;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@ApiService("detectable")
public class DetectableService {
    private final Database db;

    private static final Map<? extends TableField<DetectableRecord, ?>, Function<FieldChanges, ?>> editables = Map.of(
            DETECTABLE.DESCRIPTION, c -> c.description,
            DETECTABLE.ACTIVE, c -> c.active);

    public DetectableService(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
    }

    @ApiOperation
    public DetectableType[] listDetectableTypes() {
        return DetectableType.values();
    }
    
    @ApiOperation
    public List<DetectableInfo> listDetectables(OperatorSessionData session,
                                                String clientId,
                                                @Nullable ListFilter filter,
                                                @Nullable Set<String> with) {
        authorize(session, clientId);
        if (filter == null) filter = ListFilter.empty();

        Table<? extends Record> tables = calculateTableJoin(filter, with);
        Condition filterCondition = and(
                filterCondition(filter.detectableType, t -> DETECTABLE.DETECTABLE_TYPE.eq(t.toString())),
                filterCondition(filter.active, DETECTABLE.ACTIVE::eq),
                filterCondition(filter.assignment, ASSIGNMENT.CLIENT_REFERENCE::eq),
                filterCondition(filter.assigned, assigned ->
                        assigned ? ASSIGNMENT.CLIENT_REFERENCE.isNotNull() : ASSIGNMENT.CLIENT_REFERENCE.isNull()));
        return db.execute(sql -> sql
                .selectFrom(tables)
                .where(DETECTABLE.CLIENT_ID.eq(clientId))
                .and(filterCondition)
                .fetch(r -> new DetectableInfo(
                        clientId,
                        r.get(DETECTABLE.DETECTABLE_ID),
                        DetectableType.fromString(r.get(DETECTABLE.DETECTABLE_TYPE)),
                        r.get(DETECTABLE.DESCRIPTION),
                        fieldValueOpt(r, ASSIGNMENT.CLIENT_REFERENCE))));
    }

    @ApiOperation
    public void addDetectable(OperatorSessionData session,
                              String clientId,
                              String detectableId,
                              DetectableType detectableType,
                              String description,
                              @Nullable Boolean active,
                              @Nullable String assignment) {
        authorize(session, clientId);

        db.execute(sql -> {
            try {
                sql.insertInto(DETECTABLE)
                        .set(DETECTABLE.CLIENT_ID, clientId)
                        .set(DETECTABLE.DETECTABLE_ID, detectableId)
                        .set(DETECTABLE.DETECTABLE_TYPE, detectableType.toString())
                        .set(DETECTABLE.DESCRIPTION, description)
                        .set(DETECTABLE.ACTIVE, active != null ? active : true)
                        .execute();
                if (assignment != null) {
                    try {
                        insertIntoAssignmentQuery(sql, clientId, detectableId, detectableType, assignment).execute();
                    } catch (DataIntegrityViolationException e) {
                        throw new NotFound("holder");
                    }
                }
              } catch (DuplicateKeyException e) {
                throw new AlreadyExists("detectable");
            }
            return null;
        });
    }

    @ApiOperation
    public void editDetectable(OperatorSessionData session,
                               String clientId,
                               String detectableId,
                               DetectableType detectableType,
                               FieldChanges changes) {
        authorize(session, clientId);
        changes.validate();

        Map<? extends TableField<DetectableRecord, ?>, ?> fieldMap = getUpdateQueryFieldMap(editables, changes);

        db.execute(sql -> {
            int rowsUpdated = 0;
            if (!fieldMap.isEmpty()) {
                rowsUpdated += sql
                        .update(DETECTABLE)
                        .set(fieldMap)
                        .where(DETECTABLE.CLIENT_ID.eq(clientId))
                        .and(DETECTABLE.DETECTABLE_ID.eq(detectableId))
                        .and(DETECTABLE.DETECTABLE_TYPE.eq(detectableType.toString()))
                        .execute();
            }

            if (changes.assignment != null) {
                if (changes.assignment.isPresent()) {
                    String assignment = changes.assignment.get();
                    try {
                        rowsUpdated += insertIntoAssignmentQuery(sql, clientId, detectableId, detectableType, assignment)
                                .onConflict()
                                .doUpdate()
                                .set(ASSIGNMENT.CLIENT_REFERENCE, assignment)
                                .execute();
                    } catch (DataIntegrityViolationException e) {
                        throw new NotFound("holder");
                    }
                } else {
                    rowsUpdated++; // We silently ignore non-existent detectables and holders here for simplicity
                    sql.deleteFrom(ASSIGNMENT)
                            .where(ASSIGNMENT.CLIENT_ID.eq(clientId))
                            .and(ASSIGNMENT.DETECTABLE_ID.eq(detectableId))
                            .and(ASSIGNMENT.DETECTABLE_TYPE.eq(detectableType.toString()))
                            .execute();
                }
            }

            if (rowsUpdated == 0) {
                throw new NotFound("detectable");
            }

            return null;
        });
    }

    private static InsertOnDuplicateStep<AssignmentRecord> insertIntoAssignmentQuery(DSLContext sql,
                                                                                     String clientId,
                                                                                     String detectableId,
                                                                                     DetectableType detectableType,
                                                                                     @Nullable String assignment) {
        return sql.insertInto(ASSIGNMENT)
                .set(ASSIGNMENT.CLIENT_ID, clientId)
                .set(ASSIGNMENT.CLIENT_REFERENCE, assignment)
                .set(ASSIGNMENT.DETECTABLE_ID, detectableId)
                .set(ASSIGNMENT.DETECTABLE_TYPE, detectableType.toString());
    }

    private static Table<? extends Record> calculateTableJoin(ListFilter filter, @Nullable Set<String> with) {
        if (with == null) with = Set.of();
        Table<? extends Record> tables = DETECTABLE;
        if (with.contains("assignment") || filter.assigned.isPresent() || filter.assignment.isPresent()) {
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
        private final Optional<String> assignment;
        private final Optional<DetectableType> detectableType;
        private final Optional<Boolean> active;

        public ListFilter(Optional<Boolean> assigned,
                          Optional<String> assignment,
                          Optional<DetectableType> detectableType,
                          Optional<Boolean> active) {
            this.assigned = assigned;
            this.assignment = assignment;
            this.detectableType = detectableType;
            this.active = active;
        }

        static ListFilter empty() {
            return new ListFilter(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }
    }

    public static class DetectableInfo {
        public final String clientId;
        public final String detectableId;
        public final DetectableType detectableType;
        public final String description;
        public final Optional<String> assignment;

        public DetectableInfo(String clientId,
                              String detectableId,
                              DetectableType detectableType,
                              String description,
                              Optional<String> assignment) {
            this.clientId = clientId;
            this.detectableId = detectableId;
            this.detectableType = detectableType;
            this.description = description;
            this.assignment = assignment;
        }
    }

    public static class FieldChanges {
        public String description;
        public Boolean active;
        public Optional<String> assignment;

        public FieldChanges() {}

        void validate() {
            validateAll(
                    v("description|active|assignment", atLeastOneNonNull(description, assignment)),
                    v("description", description, Validation::shortString),
                    v("assignment",
                            assignment == null || !assignment.isPresent() ? null : shortString(assignment.get()))
            );
        }
    }
}
