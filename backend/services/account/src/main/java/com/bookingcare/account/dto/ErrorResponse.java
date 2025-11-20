package com.bookingcare.account.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        Map<String, String> validationErrors

) {


    public ErrorResponse(int status, String error, String message, String path, LocalDateTime timestamp) {
        // Gọi đến constructor chính (6 tham số) và truyền null cho validationErrors
        this(status, error, message, path, timestamp, null);
    }
}
