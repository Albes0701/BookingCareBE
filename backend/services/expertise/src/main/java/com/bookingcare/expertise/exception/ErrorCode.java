package com.bookingcare.expertise.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // ===== Doctor-related =====
    DOCTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "Doctor not found"),
    DOCTOR_ALREADY_EXISTS(HttpStatus.CONFLICT, "Doctor already exists"),
    INVALID_DOCTOR_INFORMATION(HttpStatus.BAD_REQUEST, "Invalid doctor information provided"),
    CREDENTIAL_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "Credential type not found"),
    CREDENTIAL_NOT_FOUND(HttpStatus.NOT_FOUND, "Credential not found"),

    // ===== Specialty-related =====
    SPECIALTY_NOT_FOUND(HttpStatus.NOT_FOUND, "Specialty not found"),
    SPECIALTY_ALREADY_EXISTS(HttpStatus.CONFLICT, "Specialty already exists"),
    INVALID_SPECIALTY_CODE(HttpStatus.BAD_REQUEST, "Invalid specialty code"),

    // ===== Relationship (Doctor - Specialty) =====
    DOCTOR_SPECIALTY_ALREADY_EXISTS(HttpStatus.CONFLICT, "Doctor is already linked to this specialty"),
    DOCTOR_SPECIALTY_NOT_FOUND(HttpStatus.NOT_FOUND, "Doctor-specialty relationship not found"),

    // ===== Validation / Request errors =====
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation failed"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request data"),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "Missing required field"),

    // ===== Permission / Authentication =====
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "Unauthorized access"),

    // ===== Server-side =====
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
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
