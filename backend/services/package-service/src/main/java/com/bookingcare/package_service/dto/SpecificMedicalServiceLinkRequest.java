package com.bookingcare.package_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SpecificMedicalServiceLinkRequest(
        @NotNull
        UUID specificMedicalServiceId
) {
}
