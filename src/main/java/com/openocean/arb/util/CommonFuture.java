package com.openocean.arb.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Slf4j
public class CommonFuture<V> implements Future<V> {
    private static final Cache<String, CommonFuture<?>> FUTURES;

    static {
        FUTURES = Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(30)).build();
    }

    private final String localId;

    private final CompletableFuture<V> future;

    public static <V> CommonFuture<V> register(String localId) {
        CompletableFuture<V> future = new CompletableFuture<>();
        return (CommonFuture<V>) FUTURES.get(localId, key -> new CommonFuture<>(localId, future));
    }

    public static <V> void release(String localId, V result) {
        CommonFuture<V> future = (CommonFuture<V>) FUTURES.getIfPresent(localId);
        if (future == null) {
            return;
        }
        future.getFuture().complete(result);
    }

    public static <V> CommonFuture<V> get(String localId) {
        return (CommonFuture<V>) FUTURES.getIfPresent(localId);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }
}
