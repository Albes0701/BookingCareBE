package com.bookingcare.expertise.dto;

import java.util.UUID;

import com.bookingcare.expertise.entity.VerificationAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DoctorCredentialVerificationRequestDTO(
        @NotBlank
        String doctorCredentialId,

        @NotNull
        VerificationAction action,

        UUID actorId,

        @Size(max = 2048)
        String comment
) {
}
