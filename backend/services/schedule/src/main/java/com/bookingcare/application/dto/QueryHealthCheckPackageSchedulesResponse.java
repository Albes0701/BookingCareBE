package com.bookingcare.application.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record QueryHealthCheckPackageSchedulesResponse(
    String packageScheduleId,
    String packageId,
    String scheduleId,
    LocalDate scheduleDate,
    Integer capacity,
    Integer bookedCount,
    Integer overbookLimit,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt,
    Boolean isDeleted
) {
}
