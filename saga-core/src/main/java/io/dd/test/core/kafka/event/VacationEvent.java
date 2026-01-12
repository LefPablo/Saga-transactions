package io.dd.test.core.kafka.event;

import io.dd.test.core.ProcessStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record VacationEvent(
        Long requestId,
        UUID cvUuid,
        LocalDate periodFrom,
        LocalDate periodTo,
        BigDecimal budget,
        ProcessStatus status) {
}
