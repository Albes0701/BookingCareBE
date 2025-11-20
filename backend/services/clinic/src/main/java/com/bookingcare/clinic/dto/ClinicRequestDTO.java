package com.bookingcare.clinic.dto;

import com.bookingcare.clinic.entity.ClinicStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClinicRequestDTO(
        @Size(max = 255)
        String fullname,

        @NotBlank
        @Size(max = 255)
        String name,

        @Size(max = 255)
        String address,

        String clinicDetailInfo,

        @Size(max = 255)
        String image,

        @Size(max = 255)
        String slug,

        ClinicStatus status
) {
}

