package id.unifi.service.core.services;

import com.fasterxml.jackson.core.type.TypeReference;
import id.unifi.service.common.api.Protocol;
import static id.unifi.service.common.api.SerializationUtils.getObjectMapper;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.NotFound;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.types.OperatorPK;
import id.unifi.service.core.QueryUtils;
import id.unifi.service.core.QueryUtils.ImageWithType;
import static id.unifi.service.core.QueryUtils.fieldValueOpt;
import static id.unifi.service.core.QueryUtils.filterCondition;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Keys.HOLDER_METADATA__FK_HOLDER_METADATA_TO_HOLDER;
import static id.unifi.service.core.db.Tables.HOLDER;
import static id.unifi.service.core.db.Tables.HOLDER_IMAGE;
import static id.unifi.service.core.db.Tables.HOLDER_METADATA;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Table;
import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.selectFrom;
import static org.jooq.impl.DSL.value;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ApiService("holder")
public class HolderService {
    private static final Logger log = LoggerFactory.getLogger(HolderService.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {};

    private final Database db;

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
        Condition filterCondition = and(
                filterCondition(filter.holderType, HOLDER.HOLDER_TYPE::eq),
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
    public void setImage(OperatorSessionData session, String clientId, String clientReference, byte[] image) {
        authorize(session, clientId);

        // TODO: validate image format

        db.execute(sql -> {
            if (!holderExists(clientId, clientReference, sql)) throw new NotFound("holder");

            int rowsUpdated = sql.insertInto(HOLDER_IMAGE)
                    .set(HOLDER_IMAGE.CLIENT_ID, clientId)
                    .set(HOLDER_IMAGE.CLIENT_REFERENCE, clientReference)
                    .set(HOLDER_IMAGE.IMAGE, image)
                    .onConflict()
                    .doUpdate()
                    .set(HOLDER_IMAGE.IMAGE, image)
                    .execute();

            if (rowsUpdated == 0) throw new NotFound("holder");
            return null;
        });
    }

    @ApiOperation
    public void clearImage(OperatorSessionData session, String clientId, String clientReference) {
        authorize(session, clientId);
        db.execute(sql -> {
            if (!holderExists(clientId, clientReference, sql)) throw new NotFound("holder");
            sql.deleteFrom(HOLDER_IMAGE)
                    .where(HOLDER_IMAGE.CLIENT_ID.eq(clientId))
                    .and(HOLDER_IMAGE.CLIENT_REFERENCE.eq(clientReference))
                    .execute();
            return null;
        });
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
                r.get(HOLDER.HOLDER_TYPE),
                r.get(HOLDER.ACTIVE),
                fieldValueOpt(r, HOLDER_IMAGE.IMAGE).map(QueryUtils::imageWithType),
                fieldValueOpt(r, HOLDER_METADATA.METADATA).map(HolderService::extractMetadata));
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

    @Nullable
    private static Map<String, Object> extractMetadata(Object metadata) {
        if (metadata == null) return null;

        if (!(metadata instanceof PGobject) || !((PGobject) metadata).getType().equals("jsonb")) {
            throw new IllegalArgumentException("Unexpected metadata type: " + metadata);
        }

        String metadataString = ((PGobject) metadata).getValue();

        try {
            return getObjectMapper(Protocol.JSON).readValue(metadataString, MAP_TYPE_REFERENCE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class HolderInfo {
        public final String clientReference;
        public final String name;
        public final String holderType;
        public final boolean active;
        public final Optional<ImageWithType> image;
        public final Optional<Map<String, Object>> metadata;

        public HolderInfo(String clientReference,
                          String name,
                          String holderType,
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
        public final Optional<String> holderType;
        public final Optional<Boolean> active;

        public ListFilter(Optional<String> holderType, Optional<Boolean> active) {
            this.holderType = holderType;
            this.active = active;
        }

        static ListFilter empty() {
            return new ListFilter(Optional.empty(), Optional.empty());
        }
    }
}
