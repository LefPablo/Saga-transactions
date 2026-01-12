package io.dd.test.core.kafka.command;

import java.time.LocalDate;

public record ResourcesCommand(Long requestId, LocalDate periodFrom, LocalDate periodTo) {
}
