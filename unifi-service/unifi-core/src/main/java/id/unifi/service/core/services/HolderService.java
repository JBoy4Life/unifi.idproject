package id.unifi.service.core.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.types.OperatorPK;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.HOLDER;
import static id.unifi.service.core.db.Tables.HOLDER_IMAGE;
import static id.unifi.service.core.db.Tables.HOLDER_METADATA;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Table;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.trueCondition;
import static org.jooq.impl.DSL.value;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApiService("holder")
public class HolderService {
    private static final Logger log = LoggerFactory.getLogger(HolderService.class);
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
        ListFilter effectiveFilter = filter != null ? filter : ListFilter.empty();
        Set<String> effectiveWith = with != null ? with : Set.of();
        Table<? extends Record> tables =
                effectiveWith.contains("image") ? HOLDER.leftJoin(HOLDER_IMAGE).onKey() : HOLDER;
        return db.execute(sql -> sql.selectFrom(tables)
                .where(HOLDER.CLIENT_ID.eq(clientId))
                .and(effectiveFilter.holderType.map(HOLDER.HOLDER_TYPE::eq).orElse(trueCondition()))
                .and(effectiveFilter.active.map(HOLDER.ACTIVE::eq).orElse(trueCondition()))
                .fetch(HolderService::recordToInfo));
    }

    @ApiOperation
    public HolderInfoWithMetadata getHolder(OperatorSessionData session,
                                            ObjectMapper mapper /* FIXME! */,
                                            String clientId,
                                            String clientReference) {
        authorize(session, clientId);
        return db.execute(sql -> sql.selectFrom(HOLDER.leftJoin(HOLDER_METADATA).onKey())
                .where(HOLDER.CLIENT_ID.eq(clientId))
                .and(HOLDER.CLIENT_REFERENCE.eq(clientReference))
                .fetchOne(r -> new HolderInfoWithMetadata(
                        r.get(HOLDER.CLIENT_REFERENCE),
                        r.get(HOLDER.NAME),
                        r.get(HOLDER.HOLDER_TYPE),
                        r.get(HOLDER.ACTIVE),
                        r.get(HOLDER_METADATA.METADATA),
                        mapper)));
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
                r.field(HOLDER_IMAGE.IMAGE) == null ? null : imageWithType(r.get(HOLDER_IMAGE.IMAGE)));
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

    private static OperatorPK authorize(OperatorSessionData sessionData, String clientId) {
        return Optional.ofNullable(sessionData.getOperator())
                .filter(op -> op.clientId.equals(clientId))
                .orElseThrow(Unauthorized::new);
    }

    public static class HolderInfo {
        public final String clientReference;
        public final String name;
        public final String holderType;
        public final boolean active;
        public final ImageWithType image;

        public HolderInfo(String clientReference, String name, String holderType, boolean active, ImageWithType image) {
            this.clientReference = clientReference;
            this.name = name;
            this.holderType = holderType;
            this.active = active;
            this.image = image;
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

    public class HolderInfoWithMetadata {
        public final String clientReference;
        public final String name;
        public final String holderType;
        public final boolean active;
        public final JsonNode metadata;

        public HolderInfoWithMetadata(String clientReference,
                                      String name,
                                      String holderType,
                                      boolean active,
                                      Object metadata,
                                      ObjectMapper mapper) {
            this.clientReference = clientReference;
            this.name = name;
            this.holderType = holderType;
            this.active = active;

            if (metadata == null) {
                this.metadata = NullNode.getInstance();
            } else {
                if (!(metadata instanceof PGobject) || !((PGobject) metadata).getType().equals("jsonb")) {
                    throw new IllegalArgumentException("Unexpected metadata type: " + metadata);
                }

                String metadataString = ((PGobject) metadata).getValue();

                try {
                    this.metadata = mapper.readTree(metadataString);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
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
