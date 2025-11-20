package com.bookingcare.clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClinicRejectionRequestDTO(
        @NotBlank
        @Size(max = 2048)
        String reason
) {
}
