package com.bookingcare.expertise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DoctorsRequestDTO(
        @NotBlank(message = "User ID không được để trống")
        @Size(max = 255)
        String userId,

        @Size(max = 10000)
        String doctorDetailsInfor,

        @NotBlank
        @Size(max = 255)
        String shortDoctorInfor,

        @Size(max = 255)
        String fullName,

        @NotBlank(message = "Specialty ID không được để trống")
        @Size(max = 255)
        String specialtyId
) {
}
