package com.bookingcare.expertise.dto;

import java.time.Instant;

public record CredentialTypeResponseDTO(
        String id,
        String code,
        String name,
        String description,
        boolean is_deleted,
        Instant createdAt,
        Instant updatedAt
) {
}
