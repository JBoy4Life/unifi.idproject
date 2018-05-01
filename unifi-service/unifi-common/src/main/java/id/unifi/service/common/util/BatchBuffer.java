package id.unifi.service.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

/**
 * Collects items until the buffer is full or a timeout occurs, then hands them off to the specified consumer.
 * @param <E> element type
 */
public class BatchBuffer<E> {
    private final BlockingQueue<E> queue;
    private final Thread consumerThread;
    private final Logger log;

    public static <E> BatchBuffer<E> create(String name, int size, Duration timeout, Consumer<List<E>> consumer) {
        var buffer = new BatchBuffer<>(name, size, timeout, consumer);
        buffer.start();
        return buffer;
    }

    private BatchBuffer(String name, int size, Duration timeout, Consumer<List<E>> consumer) {
        this.log = LoggerFactory.getLogger(BatchBuffer.class.getName() + ":" + name);
        this.queue = new ArrayBlockingQueue<>(size);

        this.consumerThread = new Thread(() -> {
            List<E> buffer = new ArrayList<>(size);
            while (true) {
                try {
                    Thread.sleep(timeout.toMillis());
                } catch (InterruptedException ignored) {}

                queue.drainTo(buffer, size);
                consumer.accept(buffer);
                buffer.clear();
            }
        }, "batch-buffer:" + name);
        consumerThread.setDaemon(true);
    }

    private void start() {
        consumerThread.start();
    }

    public void put(E e) throws InterruptedException {
        queue.put(e);

        if (queue.remainingCapacity() == 0) {
            synchronized (this) {
                if (queue.remainingCapacity() == 0) {
                    log.trace("Buffer queue full");
                    consumerThread.interrupt();
                }
            }
        }
    }
}
