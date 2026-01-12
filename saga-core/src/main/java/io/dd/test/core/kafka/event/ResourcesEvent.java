package io.dd.test.core.kafka.event;

public record ResourcesEvent(Long requestId, Boolean passed) {
}
