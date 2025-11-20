package com.bookingcare.expertise.dto;

import java.time.LocalDate;

import com.bookingcare.expertise.entity.CredentialStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DoctorCredentialRequestDTO(
        @NotBlank
        String doctorId,

        @NotBlank
        String credentialTypeId,

        @NotBlank
        @Size(max = 128)
        String licenseNumber,

        @NotBlank
        @Size(max = 255)
        String issuer,

        @Size(max = 2)
        String countryCode,

        @Size(max = 128)
        String region,

        LocalDate issueDate,
        LocalDate expiryDate,

        CredentialStatus status,


        @Size(max = 2048)
        String note
) {
}
