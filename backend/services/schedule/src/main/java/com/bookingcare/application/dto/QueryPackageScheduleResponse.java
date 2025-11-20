package com.bookingcare.application.dto;

import java.time.LocalDate;

public record QueryPackageScheduleResponse(
    String packageScheduleId,
    String packageId,
    QueryScheduleResponse schedule,
    LocalDate scheduleDate,
    Boolean isDeleted
) {

}
