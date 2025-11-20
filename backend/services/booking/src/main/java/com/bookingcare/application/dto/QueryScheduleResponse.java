package com.bookingcare.application.dto;

public record QueryScheduleResponse(
    String id,
    String startTime,
    String endTime,
    String dayId
) {

}
