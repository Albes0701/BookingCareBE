package com.bookingcare.clinic.dto;

public record ClinicAdminResponseDTO(
    String id,
    String name,
    String slug,
    String address,
    String clinicDetailInfo,
    String image,
    String status,
    String createdByUserId,
    Boolean isDeleted
) {
}
