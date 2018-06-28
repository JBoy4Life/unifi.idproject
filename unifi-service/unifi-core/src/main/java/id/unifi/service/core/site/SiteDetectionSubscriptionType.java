package id.unifi.service.core.site;

import id.unifi.service.common.subscriptions.SubscriptionType;
import id.unifi.service.common.subscriptions.Topic;
import id.unifi.service.common.types.pk.SitePK;

import java.util.List;

final public class SiteDetectionSubscriptionType implements SubscriptionType<List<ResolvedSiteDetection>> {
    private static final SiteDetectionSubscriptionType instance = new SiteDetectionSubscriptionType();

    private SiteDetectionSubscriptionType() {}

    public static Topic<List<ResolvedSiteDetection>> topic(SitePK site) {
        return new Topic<>(instance, site);
    }
}
