package id.unifi.service.core.verticalconfig;

import id.unifi.service.common.api.VerticalConfigForApi;
import id.unifi.service.core.db.tables.records.ClientConfigRecord;

public class CoreVerticalConfigForApi implements VerticalConfigForApi {
    private final boolean liveViewEnabled;

    public CoreVerticalConfigForApi(ClientConfigRecord record) {
        this.liveViewEnabled = record.getLiveViewEnabled();
    }

    public boolean isLiveViewEnabled() {
        return liveViewEnabled;
    }
}
