package com.bookingcare.payment.dto;

import java.time.LocalDateTime;

public record PaymentResponseDTO (
        String id, // Kiểu String để khớp với UUID
        String bookingId,
        long orderCode,
        long amount,
        String description,
        String status, // Chuyển Enum sang String
        LocalDateTime paymentDate
) {
}
