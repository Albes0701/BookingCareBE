package com.bookingcare.expertise.dto;

import java.time.Instant;
import java.util.UUID;

import com.bookingcare.expertise.entity.VerificationAction;

public record DoctorCredentialVerificationResponseDTO(
        String id,
        String doctorCredentialId,
        VerificationAction action,
        UUID actorId,
        String comment,
        Instant createdAt
) {
}
