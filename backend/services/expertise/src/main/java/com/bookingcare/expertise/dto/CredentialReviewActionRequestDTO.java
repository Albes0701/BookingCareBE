package com.bookingcare.expertise.dto;

import jakarta.validation.constraints.Size;

public record CredentialReviewActionRequestDTO(
        String actorId,
        @Size(max = 2048) String comment
) {
    
}
