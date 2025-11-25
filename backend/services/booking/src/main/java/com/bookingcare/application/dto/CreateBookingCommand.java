package com.bookingcare.application.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateBookingCommand(
    @NotBlank(message = "Booking for is mandatory")
    String bookingFor,

    String patientRelativeName,

    String patientRelativePhone,

    String doctorId,

    @NotBlank(message = "Patient ID is mandatory")
    String patientId,

    @NotBlank(message = "Patient name is mandatory")
    String patientName,

    @NotBlank(message = "Patient phone is mandatory")
    String patientPhone,

    @NotBlank(message = "Patient email is mandatory")
    String patientEmail,

    @NotBlank(message = "Patient birth date is mandatory")
    LocalDate patientBirthDate,

    @NotBlank(message = "Gender is mandatory")
    @Pattern(regexp = "^(male|female)$", message = "Gender must be male or female")
    String patientGender,

    @NotBlank(message = "Patient address is mandatory")
    String patientAddress,

    String bookingReason,

    @NotBlank(message = "Clinic branch ID is mandatory")
    String clinicBranchId,

    @NotBlank(message = "Booking package ID is mandatory")
    String bookingPackageId,

    @NotBlank(message = "Package schedule ID is mandatory")
    String packageScheduleId,

    @NotBlank(message = "Purchase method is mandatory")
    String purchaseMethod
) {
}
