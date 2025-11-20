package com.bookingcare.application.dto;

import java.time.ZonedDateTime;

public record QueryOrdersResponse(
    String id,
    String patientName,
    String healthCheckPackageName,
    String clinicName,
    String bookingStatus,
    ZonedDateTime createdDate,
    ZonedDateTime updatedDate
) {

}
