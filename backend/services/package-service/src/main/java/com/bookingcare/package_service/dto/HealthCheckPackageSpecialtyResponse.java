package com.bookingcare.package_service.dto;

import java.util.UUID;

public record HealthCheckPackageSpecialtyResponse(
        UUID packageId,
        UUID specialtyId
) {
}
