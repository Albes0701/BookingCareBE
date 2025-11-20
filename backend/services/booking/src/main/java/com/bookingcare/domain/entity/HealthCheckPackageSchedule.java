package com.bookingcare.domain.entity;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckPackageSchedule {
    private String packageScheduleId;
    private String packageId;
    private Schedule schedule;
    private LocalDate scheduleDate;
    private Boolean isDeleted;
}
