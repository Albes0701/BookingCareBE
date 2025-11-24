package com.bookingcare.payment.dto.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// PaymentSucceededEvent.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestedEvent {
    private String bookingId;
    private String patientId;
    private BigDecimal price;
    private String description;
}
