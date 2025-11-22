package com.bookingcare.infrastructure.dataaccess.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bookingcare.infrastructure.dataaccess.entity.HealthCheckPackageScheduleDoctorJpaEntity;


public interface IHealthCheckPackageScheduleDoctorJpaRepository extends JpaRepository<HealthCheckPackageScheduleDoctorJpaEntity, String>{
    @Query("SELECT h FROM HealthCheckPackageScheduleDoctorJpaEntity h WHERE h.doctorId = ?1 AND h.isDeleted = FALSE")
    List<HealthCheckPackageScheduleDoctorJpaEntity> findByDoctorId(String doctorId);

    @Query("SELECT h FROM HealthCheckPackageScheduleDoctorJpaEntity h WHERE h.packageScheduleId = ?1 AND h.doctorId = ?2 AND h.isDeleted = FALSE")
    Optional<HealthCheckPackageScheduleDoctorJpaEntity> findByPackageScheduleIdAndDoctorId(String packageScheduleId, String doctorId);


    @Query("SELECT h FROM HealthCheckPackageScheduleDoctorJpaEntity h WHERE h.packageScheduleId = ?1 AND h.isDeleted = FALSE")
    List<HealthCheckPackageScheduleDoctorJpaEntity> findByPackageScheduleId(String packageScheduleId);

    @Query("SELECT hcpsd FROM HealthCheckPackageScheduleDoctorJpaEntity hcpsd " +
       "JOIN HealthCheckPackageScheduleJpaEntity hcps ON hcpsd.packageScheduleId = hcps.packageScheduleId " +
       "JOIN ScheduleJpaEntity s ON hcps.scheduleId = s.id " +
       "WHERE hcpsd.doctorId = :doctorId " +
       "AND hcps.scheduleDate = :scheduleDate " +
       "AND hcpsd.isDeleted = FALSE " +
       "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<HealthCheckPackageScheduleDoctorJpaEntity> findByDoctorIdAndScheduleDateAndTimeSlot(
        @Param("doctorId") String doctorId,
        @Param("scheduleDate") LocalDate scheduleDate,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime);




}

