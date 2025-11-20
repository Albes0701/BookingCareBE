package com.bookingcare.expertise.dto;

import java.time.Instant;

public record DoctorCredentialFileResponseDTO(
        String id,
        String doctorCredentialId,
        String fileName,
        String contentType,
        String fileUrl,
        Instant uploadedAt
) {
}
