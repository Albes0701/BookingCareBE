package com.bookingcare.infrastructure.dataaccess.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookingcare.infrastructure.dataaccess.entity.BookingPackageDetailId;
import com.bookingcare.infrastructure.dataaccess.entity.BookingPackageDetailJpaEntity;

public interface IBookingPackageDetailJpaRepository extends JpaRepository<BookingPackageDetailJpaEntity, BookingPackageDetailId> {
    Optional<BookingPackageDetailJpaEntity> findByBookingPackageId(String bookingPackageId);
}
