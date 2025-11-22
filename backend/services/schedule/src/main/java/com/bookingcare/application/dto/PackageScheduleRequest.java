package com.bookingcare.application.dto;


import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PackageScheduleRequest(
    @NotBlank(message = "Package ID cannot be null or empty")
    String packageId,

    @NotBlank(message = "Schedule ID cannot be null or empty")
    String scheduleId,
    
    @NotBlank(message = "Date cannot be null or empty")
    String date,
    
    @NotBlank(message = "Time slot ID cannot be null or empty")
    String timeSlotId,
    
    @Positive(message = "Capacity must be greater than 0")
    int capacity

) {


    // Compact constructor for additional validation
    public PackageScheduleRequest {
        if (packageId != null) {
            packageId = packageId.trim();
        }
        if (date != null) {
            date = date.trim();
        }
        if (timeSlotId != null) {
            timeSlotId = timeSlotId.trim();
        }
        
        // Validate capacity is reasonable
        if (capacity > 10000) {
            throw new IllegalArgumentException("Capacity cannot exceed 10000");
        }
    }

    public LocalDate getScheduleDate() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getScheduleDate'");
    }

} 