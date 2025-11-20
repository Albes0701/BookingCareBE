package com.bookingcare.package_service.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    PACKAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "Health check package not found"),
    PACKAGE_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "Package type not found"),
    MEDICAL_SERVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "Medical service not found"),
    SPECIFIC_MEDICAL_SERVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "Specific medical service not found"),
    PACKAGE_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "Package name must not be blank"),
    PACKAGE_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "Package type must be provided"),
    INVALID_SLUG(HttpStatus.BAD_REQUEST, "Slug must contain alphanumeric characters"),
    SLUG_CONFLICT(HttpStatus.CONFLICT, "Slug already in use"),
    PACKAGE_NOT_EDITABLE(HttpStatus.CONFLICT, "Package cannot be edited in its current status"),
    INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "Package status transition not allowed"),
    PACKAGE_NOT_OWNED(HttpStatus.FORBIDDEN, "Package does not belong to the current owner"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),
    INVALID_STATUS_FILTER(HttpStatus.BAD_REQUEST, "Invalid package status filter"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation failed"),
    DOCTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "Doctor not found"),
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
