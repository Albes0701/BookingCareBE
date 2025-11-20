package com.bookingcare.domain.exception;

public class ScheduleDomainException extends RuntimeException {
    public ScheduleDomainException(String message) {
        super(message);
    }

    public ScheduleDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}