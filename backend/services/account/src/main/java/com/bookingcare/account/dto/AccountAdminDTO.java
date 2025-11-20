package com.bookingcare.account.dto;

public record AccountAdminDTO(
        String accountId,
        String username,
        String role,
        boolean deleted,
        String userId,
        String fullName,
        String email,
        String phone
) {
}
