package com.example.demo.commons.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public enum FailedRequestCacheService {

    INSTANCE;

    private static final Set<FailedRequest> failedRequests = new HashSet<>();

    public static void add(FailedRequest failedRequest) {
        log.info("FailedRequestCacheService::add - Adding failed request to cache: {}", failedRequest);
        failedRequests.add(failedRequest);//300
    }

    private void updateQueue() {
    }

    public record FailedRequest(
            FailedType type,
            FailedOperation operation
    ) {
    }

    public enum FailedType {
        PRODUCT
    }

    enum FailedOperation {
        CREATE,
        FIND_ALL
    }

}
