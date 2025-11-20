package com.bookingcare.infrastructure.dataaccess.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bookingcare.application.ports.output.IHealthCheckPackageScheduleBookingDetailRepository;
import com.bookingcare.domain.entity.HealthCheckPackageScheduleBookingDetail;
import com.bookingcare.infrastructure.dataaccess.entity.HealthCheckPackageScheduleBookingDetailJpaEntity;
import com.bookingcare.infrastructure.dataaccess.mapper.BookingMapperInfrastructure;
import com.bookingcare.infrastructure.dataaccess.repository.IHealthCheckPackageScheduleBookingDetailJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class HealthCheckPackageScheduleBookingDetailRepository implements IHealthCheckPackageScheduleBookingDetailRepository {
    private final IHealthCheckPackageScheduleBookingDetailJpaRepository _healthCheckPackageScheduleBookingDetailJpaRepository;
    private final BookingMapperInfrastructure mapper;

    @Override
    public HealthCheckPackageScheduleBookingDetail save(HealthCheckPackageScheduleBookingDetail order) {
        // 1. Convert domain → JPA
        HealthCheckPackageScheduleBookingDetailJpaEntity jpaEntity = mapper.toJpaEntity(order);

        // 2. Use Spring Data JPA repository
        HealthCheckPackageScheduleBookingDetailJpaEntity saved = _healthCheckPackageScheduleBookingDetailJpaRepository
                .save(jpaEntity);

        // 3. Convert JPA → domain
        return mapper.toDomain(saved);
    }

    @Override
    public List<HealthCheckPackageScheduleBookingDetail> findByPatientId(String patientId) {
        return _healthCheckPackageScheduleBookingDetailJpaRepository.findByPatientId(patientId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<HealthCheckPackageScheduleBookingDetail> findByClinicId(String patientId) {
        return _healthCheckPackageScheduleBookingDetailJpaRepository.findByClinicId(patientId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<HealthCheckPackageScheduleBookingDetail> findAll() {
        return _healthCheckPackageScheduleBookingDetailJpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<HealthCheckPackageScheduleBookingDetail> findById(String orderId) {
        return _healthCheckPackageScheduleBookingDetailJpaRepository
                .findById(orderId)
                .map(mapper::toDomain);
    }
}
