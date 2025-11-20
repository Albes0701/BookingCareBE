package com.bookingcare.account.dto;


public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        String roles,
        UserDTO user
) {

    
}
