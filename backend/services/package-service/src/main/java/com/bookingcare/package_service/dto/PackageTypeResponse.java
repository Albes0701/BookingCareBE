package com.bookingcare.package_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PackageTypeResponse(
        UUID id,
        String name,
        String image,
        String slug,
        boolean deleted,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
