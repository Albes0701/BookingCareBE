package com.bookingcare.expertise.dto;

import java.util.List;

public record DoctorCredentialWithFilesResponseDTO(
        DoctorCredentialResponseDTO credential,
        List<DoctorCredentialFileResponseDTO> files
) {
    
}
