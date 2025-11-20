package com.bookingcare.infrastructure.dataaccess.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookingcare.infrastructure.dataaccess.entity.BookingPackageDetailId;
import com.bookingcare.infrastructure.dataaccess.entity.BookingPackageDetailJpaEntity;

public interface IBookingPackageDetailJpaRepository extends JpaRepository<BookingPackageDetailJpaEntity, BookingPackageDetailId> {

}
