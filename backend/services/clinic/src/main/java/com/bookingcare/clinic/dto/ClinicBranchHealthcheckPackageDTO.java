package com.bookingcare.clinic.dto;

public record ClinicBranchHealthcheckPackageDTO(
        String id,
        String clinicBranchId,
        String healthcheckPackageId,
        boolean isDeleted
) {
}

