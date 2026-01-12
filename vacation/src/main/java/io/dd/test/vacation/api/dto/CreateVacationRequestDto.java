package io.dd.test.vacation.api.dto;

import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateVacationRequestDto(@NotNull(message = "CV uuid must be set.") UUID cvUuid,
                                       @NotNull(message = "PeriodFrom must be set.") LocalDate periodFrom,
                                       @NotNull(message = "PeriodTo must be set.") LocalDate periodTo,
                                       @Positive(message = "The budget must be a positive number.") BigDecimal budget) {
    public CreateVacationRequestDto {
        if (periodTo.isBefore(periodFrom)) {
            throw new ValidationException("PeriodTo should be equal or after periodFrom");
        }
    }
}
