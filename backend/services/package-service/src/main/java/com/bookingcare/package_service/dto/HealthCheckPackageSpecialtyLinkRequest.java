package com.bookingcare.package_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HealthCheckPackageSpecialtyLinkRequest(
        @NotNull
        UUID specialtyId
) {
}
