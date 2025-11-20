package com.bookingcare.clinic.dto;

public record DoctorsResponseDTO(
        String id,
        String userId,
        String doctorDetailsInfor,
        String shortDoctorInfor,
        String slug,
        boolean isDeleted

) {
    
}
