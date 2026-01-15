package io.dd.test.vacation.api.dto;

import io.dd.test.core.ProcessStatus;

import java.time.LocalDateTime;

public record VacationStateDto(Long requestId, ProcessStatus status, String errorMessage, LocalDateTime createdAt) {
}
