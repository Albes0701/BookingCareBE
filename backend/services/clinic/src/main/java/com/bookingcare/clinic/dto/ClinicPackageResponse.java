package com.bookingcare.clinic.dto;

public record ClinicPackageResponse(
    String id,
    String clinicBranchId,
    String healthcheckPackageId,
    boolean isDeleted
) {
    
}
