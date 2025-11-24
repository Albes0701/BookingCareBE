package com.bookingcare.domain.valueobject;

import com.bookingcare.domain.exception.BookingDomainException;

public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED;

    public static PaymentStatus fromString(String status) {
        for (PaymentStatus paymentStatus : PaymentStatus.values()) {
            if (paymentStatus.name().equalsIgnoreCase(status)) {
                return paymentStatus;
            }
        }
        throw new BookingDomainException("Invalid PaymentStatus: " + status);
    }
}
