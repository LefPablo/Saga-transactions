package io.dd.test.core.kafka.command;

import java.util.UUID;

public record ProfilerCommand(Long requestId, UUID cvUuid) {
}
