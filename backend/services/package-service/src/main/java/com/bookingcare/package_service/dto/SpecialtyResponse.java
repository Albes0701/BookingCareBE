package com.bookingcare.package_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SpecialtyResponse(
        UUID id,
        String name,
        String slug,
        String image,
        boolean deleted,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
