package io.dd.test.vacation.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = DateRangeValidator.class)
@Target({TYPE})
@Retention(RUNTIME)
public @interface ValidDateRange {
    String message() default "PeriodTo must be after or equal to PeriodFrom";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
