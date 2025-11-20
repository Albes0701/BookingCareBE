package com.bookingcare.application.dto;

public record ConfirmBookingSlotCommand(
    String scheduleHoldId,
    String bookingId
) {
}
