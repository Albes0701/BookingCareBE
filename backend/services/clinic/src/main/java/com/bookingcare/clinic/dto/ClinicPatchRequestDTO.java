package com.bookingcare.clinic.dto;

import jakarta.validation.constraints.Size;

public record ClinicPatchRequestDTO(
        @Size(max = 255)
        String fullname,

        @Size(max = 255)
        String name,

        @Size(max = 255)
        String address,

        String clinicDetailInfo,

        @Size(max = 255)
        String image,

        @Size(max = 255)
        String slug
) {
}
