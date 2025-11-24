package com.bookingcare.application.saga;

import com.bookingcare.application.dto.event.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String BOOKING_EVENTS_TOPIC = "booking-events";

    public void publishBookingCreatedEvent(BookingCreatedEvent event, String correlationId) {
        EventEnvelope<BookingCreatedEvent> envelope = EventEnvelope.of(
                "BookingCreatedEvent",
                event.getBookingId(),
                correlationId,
                "booking-service",
                event
        );

        try {
            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(BOOKING_EVENTS_TOPIC, event.getBookingId(), json);
            log.info("Published BookingCreatedEvent: bookingId={}, correlationId={}", 
                    event.getBookingId(), correlationId);
        } catch (Exception e) {
            log.error("Failed to publish BookingCreatedEvent", e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    public void publishPaymentRequestedEvent(PaymentRequestedEvent event, String correlationId) {
        EventEnvelope<PaymentRequestedEvent> envelope = EventEnvelope.of(
                "PaymentRequestedEvent",
                event.getBookingId(),
                correlationId,
                "booking-service",
                event
        );

        try {
            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send("payment-commands", event.getBookingId(), json);
            log.info("Published PaymentRequestedEvent: bookingId={}", event.getBookingId());
        } catch (Exception e) {
            log.error("Failed to publish PaymentRequestedEvent", e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    public void publishBookingConfirmedEvent(BookingConfirmedEvent event, String correlationId) {
        EventEnvelope<BookingConfirmedEvent> envelope = EventEnvelope.of(
                "BookingConfirmedEvent",
                event.getBookingId(),
                correlationId,
                "booking-service",
                event
        );

        try {
            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send("schedule-commands", event.getBookingId(), json);
            log.info("Published BookingConfirmedEvent: bookingId={}", event.getBookingId());
        } catch (Exception e) {
            log.error("Failed to publish BookingConfirmedEvent", e);
        }
    }
}