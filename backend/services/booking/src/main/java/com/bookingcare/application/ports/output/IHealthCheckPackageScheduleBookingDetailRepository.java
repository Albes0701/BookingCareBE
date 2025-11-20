package com.bookingcare.application.ports.output;

import java.util.List;
import java.util.Optional;

import com.bookingcare.domain.entity.HealthCheckPackageScheduleBookingDetail;

public interface IHealthCheckPackageScheduleBookingDetailRepository {
    HealthCheckPackageScheduleBookingDetail save(HealthCheckPackageScheduleBookingDetail bookingDetail);
    List<HealthCheckPackageScheduleBookingDetail> findAll();
    List<HealthCheckPackageScheduleBookingDetail> findByPatientId(String patientId);
    List<HealthCheckPackageScheduleBookingDetail> findByClinicId(String patientId);
    Optional<HealthCheckPackageScheduleBookingDetail> findById(String orderId);
}
