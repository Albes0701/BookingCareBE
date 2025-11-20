package com.bookingcare.account.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Username already exists"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "Password does not match"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid username or password"),

    // Validation lỗi chung
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation failed"),

    // Lỗi server
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),

    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "Unauthorized access");
    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
