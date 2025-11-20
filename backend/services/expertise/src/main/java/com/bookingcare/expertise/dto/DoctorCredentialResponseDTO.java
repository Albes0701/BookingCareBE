package com.bookingcare.expertise.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.bookingcare.expertise.entity.CredentialStatus;

public record DoctorCredentialResponseDTO(
        String id,
        String doctorId,
        String credentialTypeId,
        String licenseNumber,
        String issuer,
        String countryCode,
        String region,
        LocalDate issueDate,
        LocalDate expiryDate,
        CredentialStatus status,
        String note,
        boolean deleted,
        Instant createdAt,
        Instant updatedAt
) {
}
