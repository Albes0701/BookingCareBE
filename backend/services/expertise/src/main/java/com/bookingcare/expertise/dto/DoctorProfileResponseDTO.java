package com.bookingcare.expertise.dto;

import java.util.List;

import main.java.com.bookingcare.shared.dto.expertise.DoctorsResponseDTO;

public record DoctorProfileResponseDTO(
        DoctorsResponseDTO doctor,
        List<DoctorCredentialFullResponseDTO> credentials
) {
    
}
