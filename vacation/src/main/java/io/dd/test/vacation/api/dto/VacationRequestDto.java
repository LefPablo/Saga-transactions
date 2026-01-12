package io.dd.test.vacation.api.dto;

import io.dd.test.vacation.persistence.model.VacationRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record VacationRequestDto(Long id, UUID cvUuid, LocalDate periodFrom, LocalDate periodTo, BigDecimal budget, VacationRequestStatus status) {
}
