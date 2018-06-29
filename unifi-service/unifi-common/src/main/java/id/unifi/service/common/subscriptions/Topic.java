package id.unifi.service.common.subscriptions;

import java.util.Objects;

final public class Topic<T> {
    public final SubscriptionType<T> subscriptionType;
    public final Object subtopic;

    public Topic(SubscriptionType<T> subscriptionType, Object subtopic) {
        this.subscriptionType = subscriptionType;
        this.subtopic = subtopic;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topic<?> topic = (Topic<?>) o;
        return Objects.equals(subscriptionType, topic.subscriptionType) &&
                Objects.equals(subtopic, topic.subtopic);
    }

    public int hashCode() {
        return Objects.hash(subscriptionType, subtopic);
    }

    public String toString() {
        return "Topic{" +
                "subscriptionType=" + subscriptionType +
                ", subtopic=" + subtopic +
                '}';
    }
}
