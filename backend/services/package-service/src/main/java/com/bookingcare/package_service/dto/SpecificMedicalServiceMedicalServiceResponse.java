package com.bookingcare.package_service.dto;

import java.util.UUID;

public record SpecificMedicalServiceMedicalServiceResponse(
        UUID specificMedicalServiceId,
        UUID medicalServiceId
) {
}
