package io.dd.test.vacation.api.exception;

public class VacationServiceException extends RuntimeException {
    public VacationServiceException(String message) {
        super(message);
    }

    public VacationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
