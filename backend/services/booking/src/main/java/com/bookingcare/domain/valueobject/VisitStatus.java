package com.bookingcare.domain.valueobject;

import com.bookingcare.domain.exception.BookingDomainException;

public enum VisitStatus {
    WAITING,
    CHECKED_IN,
    COMPLETED,
    NO_SHOW,
    CANCELLED_BY_DOCTOR;

    public static VisitStatus fromString(String status) {
        for (VisitStatus visitStatus : VisitStatus.values()) {
            if (visitStatus.name().equalsIgnoreCase(status)) {
                return visitStatus;
            }
        }
        throw new BookingDomainException("Invalid VisitStatus: " + status);
    }
}
