package com.bookingcare.application.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

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