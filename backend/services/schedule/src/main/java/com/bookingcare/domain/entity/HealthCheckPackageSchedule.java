package com.bookingcare.domain.entity;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckPackageSchedule {
    private String packageScheduleId;
    private String packageId;
    private String scheduleId;
    private LocalDate scheduleDate;
    private Integer capacity;
    private Integer bookedCount;
    private Integer overbookLimit;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Boolean isDeleted;
}
