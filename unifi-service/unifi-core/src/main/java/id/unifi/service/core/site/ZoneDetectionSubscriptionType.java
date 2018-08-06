package id.unifi.service.core.site;

import id.unifi.service.common.subscriptions.SubscriptionType;
import id.unifi.service.common.subscriptions.Topic;
import id.unifi.service.common.types.pk.ZonePK;

import java.util.List;

final public class ZoneDetectionSubscriptionType implements SubscriptionType<List<ResolvedSiteDetection>> {
    private static final ZoneDetectionSubscriptionType instance = new ZoneDetectionSubscriptionType();

    private ZoneDetectionSubscriptionType() {}

    public static Topic<List<ResolvedSiteDetection>> topic(ZonePK zone) {
        return new Topic<>(instance, zone);
    }
}
