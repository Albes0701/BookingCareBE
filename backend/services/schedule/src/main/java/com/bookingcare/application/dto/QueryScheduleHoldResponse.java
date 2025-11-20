package com.bookingcare.application.dto;

import java.time.ZonedDateTime;

public record QueryScheduleHoldResponse(
    String id,
    String packageScheduleId,
    String bookingId,
    String status,
    ZonedDateTime expireAt,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {
}
