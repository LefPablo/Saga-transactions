package io.dd.test.vacation.api.dto;

import io.dd.test.core.ProcessStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record VacationRequestDto(Long id, UUID cvUuid, LocalDate periodFrom, LocalDate periodTo, BigDecimal budget, ProcessStatus status) {
}
