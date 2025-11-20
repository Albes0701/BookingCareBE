package com.bookingcare.clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClinicBranchRequestDTO(
        @NotBlank
        @Size(max = 255)
        String name,

        @Size(max = 255)
        String address
) {
}
