package com.bookingcare.payment.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
    public class ErrorResponse {
    private HttpStatus statusCode;
    private String message;
    private Map<String,String> details;

    public ErrorResponse(HttpStatus statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
    // Constructor cho exception MethodArgumentNotValidException
    public ErrorResponse(HttpStatus statusCode, Map<String, String> details) {
        this.statusCode = statusCode;
        this.details = details;
    }
}
