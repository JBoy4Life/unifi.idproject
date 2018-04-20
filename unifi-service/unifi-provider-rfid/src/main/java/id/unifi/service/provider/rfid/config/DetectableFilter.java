package id.unifi.service.provider.rfid.config;

public class DetectableFilter {
    public final UhfDetectableType detectableType;
    public final String detectableIdPrefix;

    public DetectableFilter(UhfDetectableType detectableType, String detectableIdPrefix) {
        this.detectableType = detectableType;
        this.detectableIdPrefix = detectableIdPrefix;
    }
}
