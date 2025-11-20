package com.bookingcare.infrastructure.dataaccess.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bookingcare.application.ports.output.IBookingRepository;
import com.bookingcare.domain.entity.BookingPackage;
import com.bookingcare.domain.entity.BookingPackageDetail;
import com.bookingcare.infrastructure.dataaccess.entity.BookingPackageDetailId;
import com.bookingcare.infrastructure.dataaccess.mapper.BookingMapperInfrastructure;
import com.bookingcare.infrastructure.dataaccess.repository.IBookingPackageDetailJpaRepository;
import com.bookingcare.infrastructure.dataaccess.repository.IBookingPackageJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BookingRepository implements IBookingRepository {
    private final IBookingPackageJpaRepository _bookingPackageJpaRepository;
    private final IBookingPackageDetailJpaRepository _bookingPackageDetailJpaRepository;
    private final BookingMapperInfrastructure mapper;

    @Override
    public Optional<BookingPackage> findById(String bookingPackageId) {
        return _bookingPackageJpaRepository.findById(bookingPackageId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<BookingPackageDetail> findById(String bookingPackageId,
            String healthCheckPackageId) {
        BookingPackageDetailId id = new BookingPackageDetailId(bookingPackageId, healthCheckPackageId);
        return _bookingPackageDetailJpaRepository
                .findById(id)
                .map(mapper::toDomain);
    }
}
