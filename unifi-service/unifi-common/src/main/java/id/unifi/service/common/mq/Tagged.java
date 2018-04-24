package id.unifi.service.common.mq;

public class Tagged<T> {
    public final T payload;
    public final long deliveryTag;

    public Tagged(T payload, long deliveryTag) {
        this.payload = payload;
        this.deliveryTag = deliveryTag;
    }
}
