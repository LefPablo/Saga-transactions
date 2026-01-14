package io.dd.test.vacation.api.dto;

import io.dd.test.vacation.util.ValidDateRange;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@ValidDateRange
public record CreateVacationRequestDto(@NotNull(message = "CV uuid must be set.") UUID cvUuid,
                                       @NotNull(message = "PeriodFrom must be set.") LocalDate periodFrom,
                                       @NotNull(message = "PeriodTo must be set.") LocalDate periodTo,
                                       @Positive(message = "The budget must be a positive number.") BigDecimal budget) {
}
