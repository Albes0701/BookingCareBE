package com.bookingcare.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PackageDetailResponse(
        UUID id,
        String name,
        String slug,
        String image,
        boolean deleted,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    
}
