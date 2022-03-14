package com.openocean.arb.bot.network;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author lidong
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ReconnectWebSocket implements WebSocket {

    private static final int RECONNECT_DELAY = 5;

    private final Factory factory;

    private final Request request;

    private final WebSocketListener listener;

    private final boolean autoReconnect;

    @Setter(AccessLevel.PRIVATE)
    private volatile WebSocket delegate;

    private AtomicBoolean reconnecting = new AtomicBoolean();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public Request request() {
        return delegate.request();
    }

    @Override
    public long queueSize() {
        return delegate.queueSize();
    }

    @Override
    public boolean send(String text) {
        return delegate.send(text);
    }

    @Override
    public boolean send(ByteString bytes) {
        return delegate.send(bytes);
    }

    @Override
    public boolean close(int code, String reason) {
        return delegate.close(code, reason);
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }

    public static Factory newWebSocketFactory(Factory factory, boolean autoReconnect) {
        return (request, listener) -> {
            ReconnectWebSocket webSocket = new ReconnectWebSocket(factory, request, listener,
                    autoReconnect);
            webSocket.connect();
            return webSocket;
        };
    }

    private void connect() {
        if (factory instanceof OkHttpClient) {
            ((OkHttpClient) factory).dispatcher().cancelAll();
        }
        final WebSocketListener listener = this.listener;
        final boolean reconnecting = this.reconnecting.get();
        delegate = factory.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                setDelegate(webSocket);
                if (reconnecting && listener instanceof ReconnectWebSocketListener) {
                    ((ReconnectWebSocketListener) listener).onReconnected(webSocket, response);
                } else {
                    listener.onOpen(webSocket, response);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                listener.onMessage(webSocket, text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                listener.onMessage(webSocket, bytes);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                listener.onClosing(webSocket, code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                listener.onClosed(webSocket, code, reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                listener.onFailure(webSocket, t, response);
                reconnect();
            }
        });
    }

    private void reconnect() {
        if (!autoReconnect || !reconnecting.compareAndSet(false, true)) {
            return;
        }
        log.warn("reconnecting {}", this);
        executor.schedule(() -> {
            try {
                connect();
            } finally {
                reconnecting.set(false);
            }
        }, RECONNECT_DELAY, TimeUnit.SECONDS);
    }
}
