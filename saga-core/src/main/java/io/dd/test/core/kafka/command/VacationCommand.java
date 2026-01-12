package io.dd.test.core.kafka.command;

import io.dd.test.core.ProcessStatus;

public record VacationCommand(Long requestId, ProcessStatus status) {
}
