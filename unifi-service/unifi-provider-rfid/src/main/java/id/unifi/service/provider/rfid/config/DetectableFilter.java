package id.unifi.service.provider.rfid.config;

import com.impinj.octane.TagFilterOp;

/**
 * EPC prefix detectable filter.
 */
public class DetectableFilter {
    public final String detectableIdPrefix; // hex encoded
    public final TagFilterOp action;

    public DetectableFilter(String detectableIdPrefix, TagFilterOp action) {
        this.detectableIdPrefix = detectableIdPrefix;
        this.action = action;
    }
}
