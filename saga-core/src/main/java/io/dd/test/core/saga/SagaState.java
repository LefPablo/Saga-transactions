package io.dd.test.core.saga;

import io.dd.test.core.ProcessStatus;

public record SagaState(Long requestId, ProcessStatus status, String errorMessage) {
    public SagaState(Long requestId, ProcessStatus status) {
        this(requestId, status, null);
    }
}
