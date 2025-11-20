package com.bookingcare.payment.dto;

public record PaymentLinkResponseDTO(
        long OrderCode,
        String CheckoutUrl
    ) 
{}
