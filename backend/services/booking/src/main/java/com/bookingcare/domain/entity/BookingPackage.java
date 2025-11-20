package com.bookingcare.domain.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPackage {
    private String id;
    private String name;
    private Boolean isDeleted;
    private List<BookingPackageDetail> bookingPackageDetails;
}
