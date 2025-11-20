package com.bookingcare.clinic.dto;

import com.bookingcare.clinic.entity.ClinicVerificationAction;

import java.time.Instant;
import java.util.UUID;

public record ClinicVerificationResponseDTO(
        UUID id,
        String clinicId,
        ClinicVerificationAction action,
        UUID actorId,
        String comment,
        Instant createdAt,
        boolean isDeleted
) {
}

