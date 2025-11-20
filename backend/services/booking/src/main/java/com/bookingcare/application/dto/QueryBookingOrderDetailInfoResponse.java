package com.bookingcare.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public record QueryBookingOrderDetailInfoResponse(
        String bookingId,
        String patientRelativesName,
        String patientRelativesPhoneNumber,
        String bookingReason,
        String bookingStatus,
        String purchaseMethod,
        PatientInfo patientInfo,
        ScheduleInfo scheduleInfo,
        ClinicInfo clinicInfo,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt) {
    public static record PatientInfo(
            String patientId,
            String fullName,
            String phoneNumber,
            String email,
            String address) {
    }

    public static record ScheduleInfo(
            String packageScheduleId,
            String medicalHealthCheckPackageId,
            String medicalHealthCheckPackageName,
            String scheduleTime,
            LocalDate scheduleDate,
            BookingPackageInfo bookingPackageInfo) {
        public static record BookingPackageInfo(
                String name,
                BigDecimal price) {
        }
    }

    public static record ClinicInfo(
            String clinicFullName,
            String clinicBranchName,
            String clinicBranchAddress) {
    }
}
