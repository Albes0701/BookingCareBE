package com.bookingcare.application.dto;

import java.time.LocalDate;
import java.util.List;

public record UpdateHealthCheckPackageSchedulesCommand(
        String packageId,
        List<ScheduleSlot> schedules) {

    public static record ScheduleSlot(
            String scheduleId,
            LocalDate scheduleDate,
            Integer capacity,
            Integer overbookLimit) {
    }
}
