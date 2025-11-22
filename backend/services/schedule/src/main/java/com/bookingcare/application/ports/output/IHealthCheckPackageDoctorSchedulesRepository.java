package com.bookingcare.application.ports.output;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.bookingcare.domain.entity.HealthCheckPackageScheduleDoctor;

public interface IHealthCheckPackageDoctorSchedulesRepository {
    List<HealthCheckPackageScheduleDoctor> findByDoctorId(String doctorId);
    Optional<HealthCheckPackageScheduleDoctor> findByPackageScheduleIdAndDoctorId(String packageScheduleId, String doctorId);

    HealthCheckPackageScheduleDoctor save(HealthCheckPackageScheduleDoctor doctorSchedule);

    void deleteById(String id);

    List<HealthCheckPackageScheduleDoctor> findByPackageScheduleId(String packageScheduleId);

    // Find doctors assigned to a specific package schedule at a specific time
    List<HealthCheckPackageScheduleDoctor> findByDoctorIdAndScheduleDateAndTimeSlot(
            String doctorId, 
            LocalDate scheduleDate, 
            String startTime, 
            String endTime);

}