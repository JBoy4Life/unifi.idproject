package id.unifi.service.core.services;

import com.fasterxml.jackson.core.type.TypeReference;
import id.unifi.service.common.api.Protocol;
import static id.unifi.service.common.api.SerializationUtils.getObjectMapper;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.types.OperatorPK;
import static id.unifi.service.core.QueryUtils.filterCondition;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Keys.HOLDER_METADATA__FK_HOLDER_METADATA_TO_HOLDER;
import static id.unifi.service.core.db.Tables.HOLDER;
import static id.unifi.service.core.db.Tables.HOLDER_IMAGE;
import static id.unifi.service.core.db.Tables.HOLDER_METADATA;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Table;
import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.value;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
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


    private static HolderInfo recordToInfo(Record r) {
        return new HolderInfo(
                r.get(HOLDER.CLIENT_REFERENCE),
                r.get(HOLDER.NAME),
                r.get(HOLDER.HOLDER_TYPE),
                r.get(HOLDER.ACTIVE),
                r.field(HOLDER_IMAGE.IMAGE) == null ? null : imageWithType(r.get(HOLDER_IMAGE.IMAGE)),
                r.field(HOLDER_METADATA.METADATA) == null ? null : extractMetadata(r.get(HOLDER_METADATA.METADATA)));
    }

    private static ImageWithType imageWithType(@Nullable byte[] data) {
        if (data == null) return null;
        String mimeType;
        try {
            mimeType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(data));
        } catch (IOException ignored) {
            return null;
        }

        if (mimeType == null || !mimeType.startsWith("image/")) {
            log.warn("Ignoring image of unrecognizable type: {}", mimeType);
            return null;
        }

        return new ImageWithType(mimeType, data);
    }

    private static Table<? extends Record> calculateTableJoin(@Nullable Set<String> with) {
        if (with == null) with = Set.of();

        Table<? extends Record> tables = HOLDER;
        if (with.contains("image")) {
            tables = HOLDER.leftJoin(HOLDER_IMAGE).onKey();
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
        public final ImageWithType image;
        public final Map<String, Object> metadata;

        public HolderInfo(String clientReference,
                          String name,
                          String holderType,
                          boolean active,
                          @Nullable ImageWithType image,
                          @Nullable Map<String, Object> metadata) {
            this.clientReference = clientReference;
            this.name = name;
            this.holderType = holderType;
            this.active = active;
            this.image = image;
            this.metadata = metadata;
        }
    }

    public static class ImageWithType {
        public final String mimeType;
        public final byte[] data;

        public ImageWithType(String mimeType, byte[] data) {
            this.mimeType = mimeType;
            this.data = data;
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
