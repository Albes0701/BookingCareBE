package com.bookingcare.payment.mapper;

import com.bookingcare.payment.dto.PaymentRequestCreate;
import com.bookingcare.payment.dto.PaymentResponseDTO;
import com.bookingcare.payment.entity.Payment;
import com.bookingcare.payment.entity.Status;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentMapper {
    public static Payment toPayment(PaymentRequestCreate request) {
        if (request == null) {
            return null;
        }

        // Sử dụng @Builder mà bạn đã định nghĩa trong Payment.java
        return Payment.builder()
                .bookingId(request.bookingId())
                .amount(request.amount())
                .description(request.description())
                .status(Status.PENDING) // Luôn set là PENDING khi mới tạo
                .paymentDate(LocalDateTime.now()) // Set ngày giờ tạo
                .build();
    }

    public static PaymentResponseDTO toPaymentResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        return new PaymentResponseDTO(
                payment.getId(),
                payment.getBookingId(),
                payment.getAmount(),
                payment.getOrderCode(),
                payment.getDescription(),
                payment.getStatus().name(), // Chuyển Enum sang String
                payment.getPaymentDate()
        );
    }

}
