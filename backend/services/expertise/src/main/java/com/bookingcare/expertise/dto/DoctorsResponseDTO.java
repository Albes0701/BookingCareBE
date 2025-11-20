package com.bookingcare.expertise.dto;

public record DoctorsResponseDTO(
        String id,
        String userId,
        String doctorDetailsInfor,
        String shortDoctorInfor,
        String slug,
        boolean isDeleted
) {
    
}
