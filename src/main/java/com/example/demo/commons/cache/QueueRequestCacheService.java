package com.example.demo.commons.cache;

import com.example.commons.constants.RequestStatus;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public enum QueueRequestCacheService {

    INSTANCE;

    private static final int DEFAULT_CAPACITY = 128;

    private static final ConcurrentHashMap<String, RequestStatus> cache = new ConcurrentHashMap<>(DEFAULT_CAPACITY);

    public static RequestStatus add(String key, RequestStatus value) {
        validateKey(key);
        return cache.putIfAbsent(key, value);
    }

    public static RequestStatus get(String key) {
        validateKey(key);
        return cache.get(key);
    }

    public static void update(String key, RequestStatus value) {
        validateKey(key);
        cache.replace(key, value);
        if (value == RequestStatus.ERROR) {
            FailedRequestCacheService.add(
                    new FailedRequestCacheService.FailedRequest(
                            FailedRequestCacheService.FailedType.PRODUCT,
                            FailedRequestCacheService.FailedOperation.CREATE
                    )
            );
        }
    }

    public static boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    public static RequestStatus remove(String key) {
        validateKey(key);
        return cache.remove(key);
    }

    @Scheduled(fixedRate = 300_000) // 5 minutes
    public static void clear() {
        cache.entrySet().removeIf(entry -> entry.getValue() == RequestStatus.COMPLETED);
        cache.values().removeIf(Objects::isNull);
    }

    private static void validateKey(String key) {
        Objects.requireNonNull(key, "key must not be null");
    }
}
