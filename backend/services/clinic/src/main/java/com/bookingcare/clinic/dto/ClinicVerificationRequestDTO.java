package com.bookingcare.clinic.dto;

import com.bookingcare.clinic.entity.ClinicVerificationAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ClinicVerificationRequestDTO(
        @NotBlank
        @Size(max = 36)
        String clinicId,

        @NotNull
        ClinicVerificationAction action,

        @NotNull
        UUID actorId,

        @Size(max = 2048)
        String comment,

        Boolean isDeleted
) {
}

