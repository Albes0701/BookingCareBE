package com.bookingcare.domain.exception;

public class BookingDomainException extends RuntimeException {
    public BookingDomainException(String message) {
        super(message);
    }

    public BookingDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}