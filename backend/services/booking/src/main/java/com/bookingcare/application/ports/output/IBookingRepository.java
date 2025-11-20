package com.bookingcare.application.ports.output;

import java.util.Optional;

import com.bookingcare.domain.entity.BookingPackage;
import com.bookingcare.domain.entity.BookingPackageDetail;

public interface IBookingRepository {
    Optional<BookingPackage> findById(String bookingPackageId);
    Optional<BookingPackageDetail> findById(String bookingPackageId, String healthCheckPackageId);
}
