package com.bookingcare.clinic.dto;

import com.bookingcare.clinic.entity.ClinicStatus;

public record ClinicResponseDTO(
        String id,
        String fullname,
        String name,
        String address,
        String clinicDetailInfo,
        String image,
        String slug,
        ClinicStatus status,
        boolean isDeleted
) {
}

