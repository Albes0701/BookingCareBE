package com.bookingcare.clinic.dto;

public record ClinicBranchDoctorDTO(
        String id,
        String clinicBranchId,
        String doctorId,
        boolean isDeleted
) {
}

