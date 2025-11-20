package com.bookingcare.infrastructure.dataaccess.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookingcare.infrastructure.dataaccess.entity.BookingPackageJpaEntity;

public interface IBookingPackageJpaRepository extends JpaRepository<BookingPackageJpaEntity, String> {

}
