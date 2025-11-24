package com.bookingcare.expertise.dto;

import java.util.List;


public record DoctorProfileResponseDTO(
        DoctorsResponseDTO doctor,
        List<DoctorCredentialFullResponseDTO> credentials
) {
    
}
