package com.bookingcare.clinic.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    CLINIC_NOT_FOUND(HttpStatus.NOT_FOUND, "Clinic not found"),
    CLINIC_BRANCH_NOT_FOUND(HttpStatus.NOT_FOUND, "Clinic branch not found or hidden"),
    DOCTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "Doctor not found"),
    CLINIC_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "Clinic name must not be blank"),
    INVALID_SLUG(HttpStatus.BAD_REQUEST, "Slug must contain alphanumeric characters"),
    SLUG_CONFLICT(HttpStatus.CONFLICT, "Clinic slug already in use"),
    CLINIC_NOT_EDITABLE(HttpStatus.CONFLICT, "Clinic cannot be edited in current status"),
    INVALID_STATE_TRANSITION(HttpStatus.CONFLICT, "Clinic status transition not allowed"),
    CLINIC_NOT_OWNED(HttpStatus.FORBIDDEN, "Clinic does not belong to the current user"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),
    INVALID_STATUS_FILTER(HttpStatus.BAD_REQUEST, "Invalid clinic status filter"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation failed"),
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
