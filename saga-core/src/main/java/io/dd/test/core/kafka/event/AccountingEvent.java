package io.dd.test.core.kafka.event;

public record AccountingEvent(Long requestId, Boolean allocated) {
}
