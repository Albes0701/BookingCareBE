package com.bookingcare.domain.valueobject;

import com.bookingcare.domain.exception.BookingDomainException;

public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    ABSENT,
    COMPLETED;

    public static BookingStatus fromString(String status) {
        for (BookingStatus bookingStatus : BookingStatus.values()) {
            if (bookingStatus.name().equalsIgnoreCase(status)) {
                return bookingStatus;
            }
        }
        throw new BookingDomainException("Invalid BookingStatus: " + status);
    }
}
