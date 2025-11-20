package com.bookingcare.application.ports.output;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.bookingcare.domain.entity.HealthCheckPackageSchedule;

public interface IHealthCheckPackageSchedulesRepository {
    Optional<HealthCheckPackageSchedule> findById(String packageScheduleId);
    List<HealthCheckPackageSchedule> findHealthCheckPackageSchedulesByDate(String healthCheckPackageId, LocalDate date);
    HealthCheckPackageSchedule save(HealthCheckPackageSchedule schedule);
    List<HealthCheckPackageSchedule> saveAll(List<HealthCheckPackageSchedule> schedules);
    Optional<HealthCheckPackageSchedule> findByHealthCheckPackageIdScheduleIdAndDate(String packageId, String scheduleId, LocalDate date);

    List<HealthCheckPackageSchedule> findByPackageId(String PackageId);

    List<HealthCheckPackageSchedule> findByPackageIdAndScheduleDate(String packageId, LocalDate scheduleDate);



}
