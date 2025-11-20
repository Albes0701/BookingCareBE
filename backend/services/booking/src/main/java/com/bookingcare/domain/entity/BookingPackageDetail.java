package com.bookingcare.domain.entity;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPackageDetail {
    private String packageId;
    private String bookingPackageId;
    private BookingPackage bookingPackage;
    private BigDecimal price;
    private String description;
}
