package com.bookingcare.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.bookingcare.application.dto.ApiResponse;

import java.time.LocalDateTime;

@ControllerAdvice
public class BookingDomainExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BookingDomainException.class)
    public final ResponseEntity<ApiResponse<ErrorDetails>> handleBookingDomainException(
            BookingDomainException ex, WebRequest request) {

        String message = ex.getMessage();
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            message += " Root cause: " + ex.getCause().getMessage();
        }

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                message,
                request.getDescription(false));

        ApiResponse<ErrorDetails> response = new ApiResponse<>(
                400,
                "Booking validation failed",
                errorDetails);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ApiResponse<ErrorDetails>> handleAllExceptions(
            Exception ex, WebRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));

        ApiResponse<ErrorDetails> response = new ApiResponse<>(
                500,
                "Internal server error",
                errorDetails);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static class ErrorDetails {
        private final LocalDateTime timestamp;
        private final String message;
        private final String details;

        public ErrorDetails(LocalDateTime timestamp, String message, String details) {
            this.timestamp = timestamp;
            this.message = message;
            this.details = details;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getMessage() {
            return message;
        }

        public String getDetails() {
            return details;
        }
    }
}