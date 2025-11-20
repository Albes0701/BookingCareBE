package com.bookingcare.expertise.dto;

import java.util.List;

public record DoctorCredentialFullResponseDTO(
        DoctorCredentialResponseDTO credential,
        CredentialTypeResponseDTO credentialType,
        List<DoctorCredentialFileResponseDTO> files,
        List<DoctorCredentialVerificationResponseDTO> verifications
) {
    
}
