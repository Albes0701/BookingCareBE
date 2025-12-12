package com.bookingcare.application.dto;

import java.math.BigDecimal;

import com.bookingcare.domain.valueobject.BookingStatus;

public record HealthCheckBookHistoryResponse(
    String packageScheduleId,
    String bookingPackageId,
    String booking_reason,
    BookingStatus bookingStatus,
    BigDecimal totalPrice
    
) {

} 
