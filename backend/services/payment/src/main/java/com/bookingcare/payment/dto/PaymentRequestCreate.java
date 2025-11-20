package com.bookingcare.payment.dto;

public record PaymentRequestCreate(
        String bookingId,
        int amount,
        String description
) {
}
