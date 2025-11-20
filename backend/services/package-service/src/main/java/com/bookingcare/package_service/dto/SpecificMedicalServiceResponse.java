package com.bookingcare.package_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SpecificMedicalServiceResponse(
        UUID id,
        String name,
        String image,
        String slug,
        String description,
        boolean deleted,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
