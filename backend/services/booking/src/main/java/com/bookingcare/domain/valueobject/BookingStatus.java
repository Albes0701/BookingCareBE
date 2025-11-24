package com.bookingcare.domain.valueobject;

import com.bookingcare.domain.exception.BookingDomainException;

public enum BookingStatus {
    PENDING,
    PENDING_SCHEDULE,
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED,
    EXPIRED,
    REJECTED_NO_SLOT,
    FAILED_NO_SLOT_AFTER_PAYMENT,
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
