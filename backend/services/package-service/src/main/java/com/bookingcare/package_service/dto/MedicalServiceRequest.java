package com.bookingcare.package_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicalServiceRequest(
        @NotBlank
        @Size(max = 255)
        String name,

        @Size(max = 255)
        String image,

        @NotBlank
        @Size(max = 255)
        String slug
) {
}
