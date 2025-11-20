package com.bookingcare.infrastructure.client.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HealthCheckPackageResponse(
        UUID id,
        String name,
        boolean managedByDoctor,
        String managingDoctorId,
        String image,
        UUID packageTypeId,
        String packageDetailInfo,
        String shortPackageInfo,
        String slug,
        String status,
        String rejectedReason,
        OffsetDateTime submittedAt,
        OffsetDateTime approvedAt,
        boolean deleted,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}