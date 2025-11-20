package com.bookingcare.infrastructure.dataaccess.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bookingcare.infrastructure.dataaccess.entity.HealthCheckPackageScheduleBookingDetailJpaEntity;

public interface IHealthCheckPackageScheduleBookingDetailJpaRepository extends JpaRepository<HealthCheckPackageScheduleBookingDetailJpaEntity, String> {
    @Query("SELECT h FROM HealthCheckPackageScheduleBookingDetailJpaEntity h WHERE h.patientId = ?1")
    List<HealthCheckPackageScheduleBookingDetailJpaEntity> findByPatientId(String id);

    @Query("SELECT h FROM HealthCheckPackageScheduleBookingDetailJpaEntity h WHERE h.clinicId LIKE CONCAT(?1, '%')")
    List<HealthCheckPackageScheduleBookingDetailJpaEntity> findByClinicId(String id);
}
