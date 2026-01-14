package io.dd.test.vacation.util;

import io.dd.test.vacation.api.dto.CreateVacationRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, CreateVacationRequestDto> {
    @Override
    public boolean isValid(CreateVacationRequestDto dto, ConstraintValidatorContext context) {
        return dto.periodTo().isEqual(dto.periodFrom()) || dto.periodTo().isAfter(dto.periodFrom());
    }
}
