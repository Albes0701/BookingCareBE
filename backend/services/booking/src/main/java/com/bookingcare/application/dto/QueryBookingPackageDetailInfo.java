package com.bookingcare.application.dto;

import java.math.BigDecimal;

import com.bookingcare.domain.entity.BookingPackage;

public record QueryBookingPackageDetailInfo(
        String packageId,
        String bookingPackageId,
        BookingPackage bookingPackage,
        BigDecimal price,
        String description,
        String packageName,
        String shortPackageInfo
) {
    
}
