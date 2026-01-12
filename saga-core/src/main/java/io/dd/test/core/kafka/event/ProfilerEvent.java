package io.dd.test.core.kafka.event;

public record ProfilerEvent(Long requestId, Boolean updated) {
}
