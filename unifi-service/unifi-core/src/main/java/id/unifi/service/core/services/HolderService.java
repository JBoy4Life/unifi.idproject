package id.unifi.service.core.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.operator.OperatorPK;
import id.unifi.service.common.operator.OperatorSessionData;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.HOLDER;
import static id.unifi.service.core.db.Tables.HOLDER_IMAGE;
import static id.unifi.service.core.db.Tables.HOLDER_METADATA;
import static java.lang.Boolean.TRUE;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Table;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.value;
import org.postgresql.util.PGobject;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@ApiService("holder")
public class HolderService {
    private final Database db;

    public HolderService(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
    }

    @ApiOperation
    public List<HolderInfo> listHolders(OperatorSessionData session, String clientId, @Nullable Boolean withImages) {
        authorize(session, clientId);
        Table<? extends Record> tables = TRUE.equals(withImages) ? HOLDER.leftJoin(HOLDER_IMAGE).onKey() : HOLDER;
        return db.execute(sql -> sql.selectFrom(tables)
                .where(HOLDER.CLIENT_ID.eq(clientId))
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
                r.field(HOLDER_IMAGE.IMAGE) == null ? null : r.get(HOLDER_IMAGE.IMAGE));
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
        public final byte[] image;

        public HolderInfo(String clientReference, String name, String holderType, boolean active, byte[] image) {
            this.clientReference = clientReference;
            this.name = name;
            this.holderType = holderType;
            this.active = active;
            this.image = image;
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
}
