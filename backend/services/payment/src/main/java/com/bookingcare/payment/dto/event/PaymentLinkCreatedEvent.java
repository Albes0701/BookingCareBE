package com.bookingcare.payment.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLinkCreatedEvent {
    private String bookingId;
    private String checkoutUrl;
    private Long orderCode;
}