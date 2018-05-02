package id.unifi.service.common.mq;

public class Tagged<T> {
    public final T payload;
    public final long deliveryTag;

    Tagged(T payload, long deliveryTag) {
        this.payload = payload;
        this.deliveryTag = deliveryTag;
    }

    public String toString() {
        return "Tagged{" +
                "payload=" + payload +
                ", deliveryTag=" + deliveryTag +
                '}';
    }
}
