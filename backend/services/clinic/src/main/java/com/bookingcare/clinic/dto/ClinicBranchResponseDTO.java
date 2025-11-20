package com.bookingcare.clinic.dto;

public record ClinicBranchResponseDTO(
        String id,
        String clinicId,
        String name,
        String address,
        boolean isDeleted
) {
}

