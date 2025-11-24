package com.bookingcare.domain.entity;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BookingSagaState - Saga orchestration data for booking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSagaState {
    
    private String id;
    private String bookingId;
    private String correlationId;
    
    private String sagaStatus;      // INITIATED, IN_PROGRESS, COMPLETED, FAILED
    private String currentSagaStep; // BOOKING_CREATED, SLOT_HELD, PAYMENT_COMPLETED, BOOKING_CONFIRMED
    
    // Data from schedule service
    private String scheduleHoldId;
    private ZonedDateTime holdExpireAt;
    
    // Data from payment service
    private String externalPaymentId;
    private String lastPaymentStatus; // PENDING, COMPLETED, FAILED
    
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
