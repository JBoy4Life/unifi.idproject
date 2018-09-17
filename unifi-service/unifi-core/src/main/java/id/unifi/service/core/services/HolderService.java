package id.unifi.service.core.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import id.unifi.service.common.HolderType;
import id.unifi.service.common.api.Protocol;
import static id.unifi.service.common.api.SerializationUtils.getObjectMapper;
import id.unifi.service.common.api.Validation;
import static id.unifi.service.common.api.Validation.atLeastOneNonNull;
import static id.unifi.service.common.api.Validation.v;
import static id.unifi.service.common.api.Validation.validateAll;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.AlreadyExists;
import id.unifi.service.common.api.errors.NotFound;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.types.pk.OperatorPK;
import id.unifi.service.common.util.ContentTypeUtils.ImageWithType;
import static id.unifi.service.dbcommon.DatabaseUtils.fieldValueOpt;
import static id.unifi.service.dbcommon.DatabaseUtils.filterCondition;
import static id.unifi.service.dbcommon.DatabaseUtils.getUpdateQueryFieldMap;
import static id.unifi.service.common.util.ContentTypeUtils.validateImageFormat;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Keys.HOLDER_METADATA__FK_HOLDER_METADATA_TO_HOLDER;
import static id.unifi.service.core.db.Tables.CONTACT;
import static id.unifi.service.core.db.Tables.HOLDER;
import static id.unifi.service.core.db.Tables.HOLDER_IMAGE;
import static id.unifi.service.core.db.Tables.HOLDER_METADATA;
import id.unifi.service.core.db.tables.records.HolderImageRecord;
import id.unifi.service.core.db.tables.records.HolderRecord;
import org.jooq.DSLContext;
import org.jooq.InsertOnDuplicateStep;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Table;
import org.jooq.TableField;
import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.selectFrom;
import static org.jooq.impl.DSL.value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@ApiService("holder")
public class HolderService {
    private static final Logger log = LoggerFactory.getLogger(HolderService.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {};

    private final Database db;

    private static final Map<? extends TableField<HolderRecord, ?>, Function<FieldChanges, ?>> editables = Map.of(
            HOLDER.NAME, c -> c.name,
            HOLDER.ACTIVE, c -> c.active);


    public HolderService(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
    }

    @ApiOperation
    public List<HolderInfo> listHolders(OperatorSessionData session,
                                        String clientId,
                                        @Nullable ListFilter filter,
                                        @Nullable Set<String> with) {
        authorize(session, clientId);

        if (filter == null) filter = ListFilter.empty();
        var filterCondition = and(
                filterCondition(filter.holderType, ht -> HOLDER.HOLDER_TYPE.eq(ht.toString())),
                filterCondition(filter.active, HOLDER.ACTIVE::eq));

        return db.execute(sql -> sql.selectFrom(calculateTableJoin(with))
                .where(HOLDER.CLIENT_ID.eq(clientId))
                .and(filterCondition)
                .fetch(HolderService::recordToInfo));
    }

    @ApiOperation
    public HolderInfo getHolder(OperatorSessionData session,
                                String clientId,
                                String clientReference,
                                @Nullable Set<String> with) {
        authorize(session, clientId);
        return db.execute(sql -> sql.selectFrom(calculateTableJoin(with))
                .where(HOLDER.CLIENT_ID.eq(clientId))
                .and(HOLDER.CLIENT_REFERENCE.eq(clientReference))
                .fetchOne(HolderService::recordToInfo));
    }

    @ApiOperation
    public List<String> listMetadataValues(OperatorSessionData session, String clientId, String metadataKey) {
        authorize(session, clientId);
        return db.execute(sql -> sql
                .selectDistinct(field("{0} ->> {1}", String.class, HOLDER_METADATA.METADATA, value(metadataKey)))
                .from(HOLDER.leftJoin(HOLDER_METADATA).onKey())
                .where(HOLDER.CLIENT_ID.eq(clientId))
                .fetch(Record1::value1));
    }

    @ApiOperation
    public void addHolder(OperatorSessionData session,
                          String clientId,
                          String clientReference,
                          HolderType holderType,
                          String name,
                          String note,
                          @Nullable Boolean active,
                          @Nullable byte[] image,
                          @Nullable Map<String, Object> metadata) {
        authorize(session, clientId);

        var imageWithType = validateImageFormat(Optional.ofNullable(image));

        db.execute(sql -> {
            try {
                sql.insertInto(HOLDER)
                        .set(HOLDER.CLIENT_ID, clientId)
                        .set(HOLDER.CLIENT_REFERENCE, clientReference)
                        .set(HOLDER.HOLDER_TYPE, holderType.toString())
                        .set(HOLDER.NAME, name)
                        .set(HOLDER.ACTIVE, active != null ? active : true)
                        .set(HOLDER.NOTE, note)
                        .execute();

                // TODO: validate metadata
                if (metadata != null) {
                    sql.insertInto(HOLDER_METADATA)
                            .set(HOLDER_METADATA.CLIENT_ID, clientId)
                            .set(HOLDER_METADATA.CLIENT_REFERENCE, clientReference)
                            .set(HOLDER_METADATA.METADATA, metadataToJson(metadata))
                            .execute();
                }

                switch (holderType) {
                    case CONTACT:
                        sql.insertInto(CONTACT)
                                .set(CONTACT.CLIENT_ID, clientId)
                                .set(CONTACT.CLIENT_REFERENCE, clientReference)
                                .set(CONTACT.HOLDER_TYPE, holderType.toString())
                                .execute();
                        break;
                }

                imageWithType.ifPresent(img ->
                        insertIntoHolderImageQuery(sql, clientId, clientReference, img).execute());
            } catch (DuplicateKeyException e) {
                throw new AlreadyExists("holder");
            }

            return null;
        });
    }

    @ApiOperation
    public void editHolder(OperatorSessionData session,
                           String clientId,
                           String clientReference,
                           FieldChanges changes) {
        authorize(session, clientId);
        changes.validate();

        var fieldMap = getUpdateQueryFieldMap(editables, changes);
        var imageWithType = changes.image == null ? null : validateImageFormat(changes.image);

        db.execute(sql -> {
            var rowsUpdated = 0;
            if (!fieldMap.isEmpty()) {
                rowsUpdated += sql
                        .update(HOLDER)
                        .set(fieldMap)
                        .where(HOLDER.CLIENT_ID.eq(clientId))
                        .and(HOLDER.CLIENT_REFERENCE.eq(clientReference))
                        .execute();
            }

            if (imageWithType != null) {
                if (imageWithType.isPresent()) {
                    var img = imageWithType.get();
                    rowsUpdated += insertIntoHolderImageQuery(sql, clientId, clientReference, img)
                            .onConflict()
                            .doUpdate()
                            .set(HOLDER_IMAGE.IMAGE, img.data)
                            .set(HOLDER_IMAGE.MIME_TYPE, img.mimeType)
                            .execute();
                } else {
                    rowsUpdated++; // We silently ignore non-existent holders here for simplicity
                    sql.deleteFrom(HOLDER_IMAGE)
                            .where(HOLDER_IMAGE.CLIENT_ID.eq(clientId))
                            .and(HOLDER_IMAGE.CLIENT_REFERENCE.eq(clientReference))
                            .execute();
                }
            }

            if (changes.metadata != null) {
                var jsonMetadata = metadataToJson(changes.metadata);
                rowsUpdated += sql.insertInto(HOLDER_METADATA)
                        .set(HOLDER_METADATA.CLIENT_ID, clientId)
                        .set(HOLDER_METADATA.CLIENT_REFERENCE, clientReference)
                        .set(HOLDER_METADATA.METADATA, jsonMetadata)
                        .onConflict()
                        .doUpdate()
                        .set(HOLDER_METADATA.METADATA, jsonMetadata)
                        .execute();
            }

            if (rowsUpdated == 0) throw new NotFound("holder");
            return null;
        });
    }

    private static InsertOnDuplicateStep<HolderImageRecord> insertIntoHolderImageQuery(DSLContext sql,
                                                                                       String clientId,
                                                                                       String clientReference,
                                                                                       ImageWithType image) {
        return sql.insertInto(HOLDER_IMAGE)
                .set(HOLDER_IMAGE.CLIENT_ID, clientId)
                .set(HOLDER_IMAGE.CLIENT_REFERENCE, clientReference)
                .set(HOLDER_IMAGE.IMAGE, image.data)
                .set(HOLDER_IMAGE.MIME_TYPE, image.mimeType);
    }

    private static boolean holderExists(String clientId, String clientReference, DSLContext sql) {
        return sql.fetchExists(selectFrom(HOLDER)
                .where(HOLDER.CLIENT_ID.eq(clientId))
                .and(HOLDER.CLIENT_REFERENCE.eq(clientReference)));
    }

    private static HolderInfo recordToInfo(Record r) {
        return new HolderInfo(
                r.get(HOLDER.CLIENT_REFERENCE),
                r.get(HOLDER.NAME),
                HolderType.fromString(r.get(HOLDER.HOLDER_TYPE)),
                r.get(HOLDER.ACTIVE),
                fieldValueOpt(r, HOLDER_IMAGE.IMAGE).map(i -> new ImageWithType(i, r.get(HOLDER_IMAGE.MIME_TYPE))),
                Optional.ofNullable(r.field(HOLDER_METADATA.METADATA)).map(f -> extractMetadata(r.get(f))));
    }

    private static Table<? extends Record> calculateTableJoin(@Nullable Set<String> with) {
        if (with == null) with = Set.of();

        Table<? extends Record> tables = HOLDER;
        if (with.contains("image")) {
            tables = tables.leftJoin(HOLDER_IMAGE).onKey();
        }
        if (with.contains("metadata")) {
            tables = tables.leftJoin(HOLDER_METADATA).onKey(HOLDER_METADATA__FK_HOLDER_METADATA_TO_HOLDER);
        }

        return tables;
    }

    private static OperatorPK authorize(OperatorSessionData sessionData, String clientId) {
        return Optional.ofNullable(sessionData.getOperator())
                .filter(op -> op.clientId.equals(clientId))
                .orElseThrow(Unauthorized::new);
    }

    private static Map<String, Object> extractMetadata(JsonNode metadata) {
        return metadata == null ? Map.of() : getObjectMapper(Protocol.JSON).convertValue(metadata, MAP_TYPE_REFERENCE);
    }

    private static JsonNode metadataToJson(Map<String, Object> metadata) {
        return getObjectMapper(Protocol.JSON).valueToTree(metadata);
    }

    public static class HolderInfo {
        public final String clientReference;
        public final String name;
        public final HolderType holderType;
        public final boolean active;
        public final Optional<ImageWithType> image;
        public final Optional<Map<String, Object>> metadata;

        public HolderInfo(String clientReference,
                          String name,
                          HolderType holderType,
                          boolean active,
                          Optional<ImageWithType> image,
                          Optional<Map<String, Object>> metadata) {
            this.clientReference = clientReference;
            this.name = name;
            this.holderType = holderType;
            this.active = active;
            this.image = image;
            this.metadata = metadata;
        }
    }

    public static class ListFilter {
        public final Optional<HolderType> holderType;
        public final Optional<Boolean> active;

        public ListFilter(Optional<HolderType> holderType, Optional<Boolean> active) {
            this.holderType = holderType;
            this.active = active;
        }

        static ListFilter empty() {
            return new ListFilter(Optional.empty(), Optional.empty());
        }
    }

    public static class FieldChanges {
        public String name;
        public Boolean active;
        public Optional<byte[]> image;
        public Map<String, Object> metadata;

        public FieldChanges() {}

        void validate() {
            validateAll(
                    v("name|active|image|metadata", atLeastOneNonNull(name, active, image, metadata)),
                    v("name", name, Validation::shortString)
            );
        }
    }
}
