package id.unifi.service.common.api.client;

import com.fasterxml.jackson.core.type.TypeReference;
import id.unifi.service.common.api.Dispatcher.CancellableWireMessageListener;
import id.unifi.service.common.api.Dispatcher.WireMessageListener;
import id.unifi.service.common.api.Message;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class ClientUtils {
    public static CancellableWireMessageListener oneOffWireMessageListener(WireMessageListener listener) {
        return (om, session, message) -> {
            listener.accept(om, session, message);
            return false;
        };
    }

    public static <T> Future<T> awaitResponse(String responseMessageType,
                                              //TypeReference<T> typeReference,
                                              Consumer<CancellableWireMessageListener> dispatcherCall) {
        var future = new CompletableFuture<T>();

        TypeReference<Object> typeReference = new TypeReference<>() {}; // TODO: investigate

        dispatcherCall.accept((om, session, message) -> {
            try {
                if (message.messageType.equals(responseMessageType)) {
                    future.complete(om.readValue(om.treeAsTokens(message.payload), typeReference));
                } else {
                    future.completeExceptionally(unmarshalError(message));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return false;
        });

        return future;
    }

    private static UnmarshalledError unmarshalError(Message message) {
        return new UnmarshalledError(message.messageType, message.payload.get("message").textValue());
    }
}
