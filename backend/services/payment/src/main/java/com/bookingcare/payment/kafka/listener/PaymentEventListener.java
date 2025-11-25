package com.bookingcare.payment.kafka.listener;

import com.bookingcare.payment.dto.event.EventEnvelope;
import com.bookingcare.payment.dto.event.PaymentFailedEvent;
import com.bookingcare.payment.dto.event.PaymentLinkCreatedEvent;
import com.bookingcare.payment.dto.event.PaymentRequestedEvent;
import com.bookingcare.payment.dto.event.PaymentSucceededEvent;
import com.bookingcare.payment.dto.CreatePaymentLinkRequestBody;
import com.bookingcare.payment.dto.PaymentRequestCreate;
import com.bookingcare.payment.dto.PaymentResponseDTO;
import com.bookingcare.payment.service.interfaceService.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Step 4: Handle PaymentRequestedEvent from Booking Service
     */
    @KafkaListener(topics = "payment-commands", groupId = "payment-group")
    @Transactional
    public void handlePaymentCommands(String jsonMessage) {
        try {
            EventEnvelope<?> envelope = objectMapper.readValue(jsonMessage, EventEnvelope.class);
            
            log.info("Payment received command: type={}, aggregateId={}", 
                    envelope.getEventType(), envelope.getAggregateId());

            if ("PaymentRequestedEvent".equals(envelope.getEventType())) {
                handlePaymentRequested(envelope);
            }
            
            // ✅ KHÔNG throw exception ra ngoài
            
        } catch (Exception e) {
            log.error("========================================");
            log.error("❌ FATAL ERROR in handlePaymentCommands");
            log.error("Message: {}", jsonMessage);
            log.error("Error: {}", e.getMessage(), e);
            log.error("========================================");
            
            // ❌ KHÔNG throw - sẽ làm Kafka retry
            // throw new RuntimeException(e); // XÓA dòng này
            
            // ✅ Log error và ACK message để Kafka KHÔNG retry
        }
    }

    // @Transactional
    // private void handlePaymentRequested(EventEnvelope<?> envelope) {
    //     try {
    //         PaymentRequestedEvent event = objectMapper.convertValue(
    //                 envelope.getPayload(), PaymentRequestedEvent.class);

    //         log.info("Processing payment: bookingId={}, amount={}", 
    //                 event.getBookingId(), event.getPrice());

    //         // Create payment record
    //         PaymentRequestCreate paymentRequest = new PaymentRequestCreate(
    //                 event.getBookingId(),
    //                 event.getPrice().intValue(), // Convert BigDecimal to int
    //                 event.getDescription()
    //         );
            
    //         // ✅ Service tạo Payment với status=PENDING
    //         PaymentResponseDTO paymentResponse = paymentService.CreatePaymentAsync(paymentRequest);

    //         log.info("Payment created: paymentId={}, bookingId={}, status={}", 
    //                 paymentResponse.id(), event.getBookingId(), paymentResponse.status());

    //         // ✅ CHECK status từ response
    //         if ("PENDING".equals(paymentResponse.status())) {
                
    //             log.info("Payment is PENDING - waiting for PayOS webhook: paymentId={}", 
    //                     paymentResponse.id());
                
    //             // ⚠️ DON'T publish success yet
    //             // Webhook từ PayOS sẽ call publishPaymentSuccessEvent() sau khi customer thanh toán
                
    //         } else if ("COMPLETED".equals(paymentResponse.status())) {
                
    //             log.info("Payment already COMPLETED: paymentId={}", paymentResponse.id());
                
    //             // ✅ Publish success (rare case)
    //             publishPaymentSuccessEvent(
    //                     event.getBookingId(),
    //                     paymentResponse.id(),
    //                     "TXN_" + System.currentTimeMillis(),
    //                     envelope.getCorrelationId()
    //             );
                
    //         } else {
    //             log.warn("Payment in unexpected status: {}", paymentResponse.status());
    //         }

    //     } catch (Exception e) {
    //         log.error("Payment processing failed for booking: {}", envelope.getAggregateId(), e);
            
    //         // Publish PaymentFailedEvent
    //         publishPaymentFailureEvent(
    //                 envelope.getAggregateId(),
    //                 "Exception: " + e.getMessage(),
    //                 envelope.getCorrelationId()
    //         );
    //     }
    // }


    @Transactional
    private void handlePaymentRequested(EventEnvelope<?> envelope) {
        try {
            PaymentRequestedEvent event = objectMapper.convertValue(
                    envelope.getPayload(), PaymentRequestedEvent.class);

            log.info("========================================");
            log.info("💰 PROCESSING PAYMENT REQUEST");
            log.info("Booking ID: {}", event.getBookingId());
            log.info("Amount: {}", event.getPrice());
            log.info("Description: {}", event.getDescription());
            log.info("========================================");

            // ✅ STEP 1: VALIDATE description
            if (event.getDescription() == null || event.getDescription().isEmpty()) {
                log.error("❌ Invalid description: null or empty");
                publishPaymentFailureEvent(
                        event.getBookingId(),
                        "Invalid payment request: description is required",
                        envelope.getCorrelationId()
                );
                return; // ✅ Exit sớm, KHÔNG retry
            }

            // ✅ STEP 2: TRUNCATE description
            String description = event.getDescription();
            if (description.length() > 25) {
                description = description.substring(0, 25);
                log.warn("⚠️ Description truncated from {} to 25 chars", event.getDescription().length());
            }
            
            String productName = "Booking #" + event.getBookingId();
            if (productName.length() > 25) {
                productName = productName.substring(0, 25);
            }

            log.info("✅ STEP 2: Validation passed");
            log.info("  - Product Name: {} ({} chars)", productName, productName.length());
            log.info("  - Description: {} ({} chars)", description, description.length());

            // ✅ STEP 3: BUILD request cho PayOS
            CreatePaymentLinkRequestBody linkRequest = new CreatePaymentLinkRequestBody(
                    event.getBookingId(),
                    productName,
                    description,
                    "https://your-frontend.com/success?id=" + event.getBookingId(),
                    event.getPrice().intValue(),
                    "https://your-frontend.com/cancel?id=" + event.getBookingId()
            );
            
            log.info("📞 STEP 3: Calling PayOS API...");
            
            // ✅ STEP 4: CALL createPaymentLink (lưu 1 lần duy nhất!)
            CreatePaymentLinkResponse paymentLink = paymentService.createPaymentLink(linkRequest);
            
            log.info("========================================");
            log.info("✅✅✅ PAYOS SUCCESS ✅✅✅");
            log.info("Checkout URL: {}", paymentLink.getCheckoutUrl());
            log.info("Order Code: {}", paymentLink.getOrderCode());
            log.info("QR Code: {}", paymentLink.getQrCode());
            log.info("========================================");

            // ❌ KHÔNG gọi CreatePaymentAsync() nữa!
            // createPaymentLink() đã lưu payment vào DB rồi!
            
            log.info("✅ STEP 5: Publishing PaymentLinkCreatedEvent...");

            // ✅ STEP 6: PUBLISH event
            publishPaymentLinkCreatedEvent(
                    event.getBookingId(),
                    paymentLink.getCheckoutUrl(),
                    paymentLink.getOrderCode(),
                    envelope.getCorrelationId()
            );
            
            log.info("========================================");
            log.info("✅ Payment link created successfully");
            log.info("Waiting for PayOS webhook...");
            log.info("========================================");

        } catch (Exception e) {
            log.error("========================================");
            log.error("❌ PAYMENT LINK CREATION FAILED");
            log.error("Booking ID: {}", envelope.getAggregateId());
            log.error("Error Type: {}", e.getClass().getSimpleName());
            log.error("Error Message: {}", e.getMessage());
            log.error("========================================");
            
            // ✅ PUBLISH failure event
            publishPaymentFailureEvent(
                    envelope.getAggregateId(),
                    "PayOS API error: " + e.getMessage(),
                    envelope.getCorrelationId()
            );
            
            // ✅ KHÔNG throw exception - Kafka sẽ ACK message
        }
    }

    // ✅ THÊM method mới để publish event
    @Transactional
    private void publishPaymentLinkCreatedEvent(String bookingId, String checkoutUrl, 
                                                long orderCode, String correlationId) {
        try {
            log.info("Publishing PaymentLinkCreatedEvent: bookingId={}, url={}", bookingId, checkoutUrl);
            
            PaymentLinkCreatedEvent event = PaymentLinkCreatedEvent.builder()
                    .bookingId(bookingId)
                    .checkoutUrl(checkoutUrl)
                    .orderCode(orderCode)
                    .build();

            publishPaymentEvent("PaymentLinkCreatedEvent", bookingId, correlationId, event);
            
        } catch (Exception e) {
            log.error("Failed to publish payment link created event", e);
        }
    }

    /**
     * ✅ NEW: Called from PaymentWebhookController when PayOS webhook succeeds
     * (Customer đã thanh toán xong)
     */
    @Transactional
    public void publishPaymentSuccessEvent(String bookingId, String paymentId, 
                                           String transactionId, String correlationId) {
        try {
            log.info("Publishing PaymentSucceededEvent: bookingId={}, paymentId={}", 
                    bookingId, paymentId);
            
            PaymentSucceededEvent successEvent = PaymentSucceededEvent.builder()
                    .bookingId(bookingId)
                    .paymentId(paymentId)
                    .transactionId(transactionId)
                    .build();

            publishPaymentEvent("PaymentSucceededEvent", bookingId, correlationId, successEvent);
            
            log.info("PaymentSucceededEvent published successfully");
            
        } catch (Exception e) {
            log.error("Failed to publish payment success event", e);
            throw new RuntimeException("Failed to publish PaymentSucceededEvent", e);
        }
    }

    /**
     * ✅ NEW: Called from PaymentWebhookController when PayOS webhook fails
     */
    @Transactional
    public void publishPaymentFailureEvent(String bookingId, String reason, String correlationId) {
        try {
            log.info("Publishing PaymentFailedEvent: bookingId={}, reason={}", bookingId, reason);
            
            PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                    .bookingId(bookingId)
                    .reason(reason)
                    .build();

            publishPaymentEvent("PaymentFailedEvent", bookingId, correlationId, failedEvent);
            
            log.info("PaymentFailedEvent published successfully");
            
        } catch (Exception e) {
            log.error("Failed to publish payment failure event", e);
            throw new RuntimeException("Failed to publish PaymentFailedEvent", e);
        }
    }

    @Transactional
    private void publishPaymentEvent(String eventType, String aggregateId, 
                                      String correlationId, Object payload) {
        try {
            log.info("========================================");
            log.info("📤 PUBLISHING TO KAFKA");
            log.info("Topic: payment-events");
            log.info("Event Type: {}", eventType);
            log.info("Aggregate ID: {}", aggregateId);
            log.info("Correlation ID: {}", correlationId);
            log.info("========================================");
            
            EventEnvelope<Object> envelope = EventEnvelope.of(
                    eventType,
                    aggregateId,
                    correlationId,
                    "payment-service",
                    payload
            );

            String json = objectMapper.writeValueAsString(envelope);
            
            log.info("📦 JSON Payload: {}", json);
            
            kafkaTemplate.send("payment-events", aggregateId, json);
            
            log.info("========================================");
            log.info("✅ EVENT PUBLISHED SUCCESSFULLY");
            log.info("Event Type: {}", eventType);
            log.info("========================================");
        } catch (Exception e) {
            log.error("========================================");
            log.error("❌ FAILED TO PUBLISH EVENT");
            log.error("Event Type: {}", eventType);
            log.error("Error: {}", e.getMessage(), e);
            log.error("========================================");
            throw new RuntimeException("Failed to publish payment event", e);
        }
    }

}