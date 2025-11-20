package com.bookingcare.expertise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DoctorCredentialFileRequestDTO(
        @NotBlank
        String doctorCredentialId,

        @NotBlank
        @Size(max = 255)
        String fileName,

        @Size(max = 100)
        String contentType,

        @NotBlank
        @Size(max = 2048)
        String fileUrl
) {
}
