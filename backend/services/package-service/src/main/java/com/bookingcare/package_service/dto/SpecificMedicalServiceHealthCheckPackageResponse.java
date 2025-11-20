package com.bookingcare.package_service.dto;

import java.util.UUID;

public record SpecificMedicalServiceHealthCheckPackageResponse(
        UUID specificMedicalServiceId,
        UUID packageId
) {
}
