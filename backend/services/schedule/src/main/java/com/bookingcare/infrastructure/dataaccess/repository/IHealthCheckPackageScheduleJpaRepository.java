package com.bookingcare.infrastructure.dataaccess.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bookingcare.infrastructure.dataaccess.entity.HealthCheckPackageScheduleJpaEntity;

public interface IHealthCheckPackageScheduleJpaRepository extends JpaRepository<HealthCheckPackageScheduleJpaEntity, String> {
    @Query("SELECT h FROM HealthCheckPackageScheduleJpaEntity h WHERE h.packageId = :healthCheckPackageId AND h.scheduleDate = :date")
    List<HealthCheckPackageScheduleJpaEntity> findByHealthCheckPackageIdAndDate(String healthCheckPackageId, LocalDate date);

    @Query("SELECT h FROM HealthCheckPackageScheduleJpaEntity h WHERE h.packageId = :packageId AND h.scheduleId = :scheduleId AND h.scheduleDate = :date")
    Optional<HealthCheckPackageScheduleJpaEntity> findByHealthCheckPackageIdScheduleIdAndDate(String packageId, String scheduleId, LocalDate date);


    List<HealthCheckPackageScheduleJpaEntity> findByPackageIdAndScheduleDate(String packageId, LocalDate scheduleDate);

    List<HealthCheckPackageScheduleJpaEntity> findByPackageId(String packageId);


}
