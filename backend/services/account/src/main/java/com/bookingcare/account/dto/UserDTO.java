package com.bookingcare.account.dto;

import com.bookingcare.account.entity.Gender;

import java.time.LocalDate;

public record UserDTO(
        String id,
        String fullName,
        LocalDate dateOfBirth,
        String email,
        String phoneNumber,
        String address,
        Gender gender,
        String imageUrl,
        boolean isDeleted

) {
}
