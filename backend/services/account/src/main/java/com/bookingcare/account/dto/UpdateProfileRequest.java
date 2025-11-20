package com.bookingcare.account.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @NotBlank(message = "Fullname cannot be empty")
        @Size(min = 2, max = 100, message = "Fullname must be between 2 and 100 characters")
        String fullname,

        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email format is not valid")
        String email,

        @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be 10 or 11 digits")
        String phone,

        @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address,

        @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
        String gender,

        String image, // Thường không validate trực tiếp, logic xử lý file/URL sẽ nằm ở service

        @Past(message = "Birthdate must be in the past")
        LocalDate birthdate
) {
}
