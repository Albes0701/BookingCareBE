package com.bookingcare.application.dto;

public record UpdateBookingOrderStatusCommand(
    String orderId,
    String status
) {
}
