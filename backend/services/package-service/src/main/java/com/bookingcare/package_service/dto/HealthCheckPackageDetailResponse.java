package com.bookingcare.package_service.dto;

import java.util.List;

public record HealthCheckPackageDetailResponse(
        HealthCheckPackageResponse healthCheckPackage,
        List<SpecificMedicalServiceResponse> specificMedicalServices
) {
}
