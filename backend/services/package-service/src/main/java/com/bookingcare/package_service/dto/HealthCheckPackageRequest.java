package com.bookingcare.package_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HealthCheckPackageRequest(
        @NotBlank
        @Size(max = 255)
        String name,

        Boolean managedByDoctor,

        @Size(max = 255)
        String managingDoctorId,

        @Size(max = 255)
        String image,

        @NotNull
        UUID packageTypeId,

        String packageDetailInfo,

        String shortPackageInfo,

        @NotBlank
        @Size(max = 255)
        String slug,

        OffsetDateTime submittedAt,

        OffsetDateTime approvedAt

) {
}
