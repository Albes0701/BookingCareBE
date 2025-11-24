package com.bookingcare.payment.controller;

import com.bookingcare.payment.dto.PaymentResponseDTO;
import com.bookingcare.payment.dto.common.ApiResponse;
import com.bookingcare.payment.kafka.listener.PaymentEventListener;
import com.bookingcare.payment.service.interfaceService.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.model.webhooks.WebhookData;

@RestController
@RequestMapping("api/v1/webhooks/callback")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {
    private final PayOS payOS;
    private final PaymentService paymentService;
    private final PaymentEventListener paymentEventListener;  // ✅ Thêm
    
    @PostMapping()
    public ApiResponse<WebhookData> payosTransferHandler(@RequestBody Object body)
            throws JsonProcessingException, IllegalArgumentException {
        try {
            // 1. Verify webhook signature từ PayOS
            WebhookData data = payOS.webhooks().verify(body);
            log.info("PayOS webhook verified: code={}, desc={}, orderCode={}", 
                    data.getCode(), data.getDesc(), data.getOrderCode());
            
            // 2. Cập nhật payment status trong database
            paymentService.UpdatePaymentStatusByWebhook(data);
            
            // ✅ Get payment info từ DB (chỉ gọi 1 lần)
            PaymentResponseDTO paymentResponse = paymentService.GetPaymentByOrderCodeAsync(data.getOrderCode());
            String bookingId = paymentResponse.bookingId();
            String paymentId = paymentResponse.id();
            
            log.info("Payment retrieved: bookingId={}, paymentId={}, status={}", 
                    bookingId, paymentId, paymentResponse.status());
            
            // ✅ Check webhook status từ PayOS
            if ("00".equals(data.getCode())) {
                
                // ✅ Payment SUCCESS - publish success event
                log.info("✅ Payment success: bookingId={}, paymentId={}", bookingId, paymentId);
                
                // ✅ Generate transactionId từ PayOS data
                String transactionId = "TXN_" + data.getOrderCode() + "_" + System.currentTimeMillis();


                paymentEventListener.publishPaymentSuccessEvent(
                        bookingId,
                        paymentId,
                        transactionId,
                        "webhook-" + data.getOrderCode()  // ✅ Dùng orderCode làm correlationId
                );
                
            } else {
                
                // ❌ Payment FAILED - publish failure event
                log.warn("❌ Payment failed: bookingId={}, code={}, reason={}", 
                        bookingId, data.getCode(), data.getDesc());
                
                paymentEventListener.publishPaymentFailureEvent(
                        bookingId,
                        "Payment failed - Code: " + data.getCode() + ", Reason: " + data.getDesc(),
                        "webhook-" + data.getOrderCode()
                );
            }
            
            return ApiResponse.success("Webhook delivered", data);
            
        } catch (Exception e) {
            log.error("Error handling PayOS webhook: {}", e.getMessage(), e);
            return ApiResponse.error("Webhook processing failed: " + e.getMessage());
        }
    }
}
