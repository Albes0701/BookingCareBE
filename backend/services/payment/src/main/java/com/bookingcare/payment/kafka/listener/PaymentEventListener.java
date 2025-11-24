package com.bookingcare.payment.kafka.listener;

import com.bookingcare.payment.dto.event.EventEnvelope;
import com.bookingcare.payment.dto.event.PaymentFailedEvent;
import com.bookingcare.payment.dto.event.PaymentRequestedEvent;
import com.bookingcare.payment.dto.event.PaymentSucceededEvent;
import com.bookingcare.payment.dto.PaymentRequestCreate;
import com.bookingcare.payment.dto.PaymentResponseDTO;
import com.bookingcare.payment.service.interfaceService.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        } catch (Exception e) {
            log.error("Error handling payment command", e);
        }
    }

    private void handlePaymentRequested(EventEnvelope<?> envelope) {
        try {
            PaymentRequestedEvent event = objectMapper.convertValue(
                    envelope.getPayload(), PaymentRequestedEvent.class);

            log.info("Processing payment: bookingId={}, amount={}", 
                    event.getBookingId(), event.getPrice());

            // Create payment record
            PaymentRequestCreate paymentRequest = new PaymentRequestCreate(
                    event.getBookingId(),
                    event.getPrice().intValue(), // Convert BigDecimal to int
                    event.getDescription()
            );
            
            // ✅ Service tạo Payment với status=PENDING
            PaymentResponseDTO paymentResponse = paymentService.CreatePaymentAsync(paymentRequest);

            log.info("Payment created: paymentId={}, bookingId={}, status={}", 
                    paymentResponse.id(), event.getBookingId(), paymentResponse.status());

            // ✅ CHECK status từ response
            if ("PENDING".equals(paymentResponse.status())) {
                
                log.info("Payment is PENDING - waiting for PayOS webhook: paymentId={}", 
                        paymentResponse.id());
                
                // ⚠️ DON'T publish success yet
                // Webhook từ PayOS sẽ call publishPaymentSuccessEvent() sau khi customer thanh toán
                
            } else if ("COMPLETED".equals(paymentResponse.status())) {
                
                log.info("Payment already COMPLETED: paymentId={}", paymentResponse.id());
                
                // ✅ Publish success (rare case)
                publishPaymentSuccessEvent(
                        event.getBookingId(),
                        paymentResponse.id(),
                        "TXN_" + System.currentTimeMillis(),
                        envelope.getCorrelationId()
                );
                
            } else {
                log.warn("Payment in unexpected status: {}", paymentResponse.status());
            }

        } catch (Exception e) {
            log.error("Payment processing failed for booking: {}", envelope.getAggregateId(), e);
            
            // Publish PaymentFailedEvent
            publishPaymentFailureEvent(
                    envelope.getAggregateId(),
                    "Exception: " + e.getMessage(),
                    envelope.getCorrelationId()
            );
        }
    }

    /**
     * ✅ NEW: Called from PaymentWebhookController when PayOS webhook succeeds
     * (Customer đã thanh toán xong)
     */
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

    private void publishPaymentEvent(String eventType, String aggregateId, 
                                      String correlationId, Object payload) {
        try {
            EventEnvelope<Object> envelope = EventEnvelope.of(
                    eventType,
                    aggregateId,
                    correlationId,
                    "payment-service",
                    payload
            );

            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send("payment-events", aggregateId, json);
            
            log.info("Published {}: aggregateId={}, correlationId={}", 
                    eventType, aggregateId, correlationId);
        } catch (Exception e) {
            log.error("Failed to publish payment event: {}", eventType, e);
            throw new RuntimeException("Failed to publish payment event", e);
        }
    }
}