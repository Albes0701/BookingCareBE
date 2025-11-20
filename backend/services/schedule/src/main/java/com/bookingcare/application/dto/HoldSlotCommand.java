package com.bookingcare.application.dto;

import java.time.ZonedDateTime;

public record HoldSlotCommand(
    String packageScheduleId,
    String bookingId,
    ZonedDateTime expireAt
) {
}
