package com.bookingcare.infrastructure.messaging.listener;

import com.bookingcare.application.dto.event.BookingConfirmedEvent;
import com.bookingcare.application.dto.event.BookingCreatedEvent;
import com.bookingcare.application.dto.event.EventEnvelope;
import com.bookingcare.application.ports.input.IScheduleApplicationServicePatient;
import com.bookingcare.infrastructure.messaging.event.HoldSlotFailedEvent;
import com.bookingcare.infrastructure.messaging.event.HoldSlotSucceededEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final IScheduleApplicationServicePatient scheduleService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Step 3: Handle BookingCreatedEvent - Hold slot
     */
    @KafkaListener(topics = "booking-events", groupId = "schedule-group")
    @Transactional
    public void handleBookingEvents(String jsonMessage) {
        try {
            log.info("Received raw message: {}", jsonMessage);

            // Handle potential double-serialization
            JsonNode rootNode = objectMapper.readTree(jsonMessage);
            if (rootNode.isTextual()) {
                rootNode = objectMapper.readTree(rootNode.asText());
            }

            // Convert to EventEnvelope
            EventEnvelope<?> envelope = objectMapper.treeToValue(rootNode, EventEnvelope.class);

            String eventType = envelope.getEventType();
            String aggregateId = envelope.getAggregateId();
            String correlationId = envelope.getCorrelationId();
            Object payload = envelope.getPayload();

            log.info("Schedule received event: type={}, aggregateId={}", eventType, aggregateId);

            if ("BookingCreatedEvent".equals(eventType) && payload != null) {
                // Convert payload to JsonNode for easier handling
                var payloadNode = objectMapper.valueToTree(payload);
                handleBookingCreated(correlationId, payloadNode);
            } else {
                log.warn("Unknown event type or missing payload: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error handling booking event", e);
        }
    }

    private void handleBookingCreated(String correlationId, com.fasterxml.jackson.databind.JsonNode payloadNode) {
        try {
            BookingCreatedEvent event = objectMapper.convertValue(payloadNode, BookingCreatedEvent.class);

            log.info("Attempting to hold slot: bookingId={}, packageScheduleId={}",
                    event.getBookingId(), event.getPackageScheduleId());

            // Call domain service to hold slot - returns holdId (String)
            String holdId = scheduleService.holdScheduleForBooking(
                    event.getPackageScheduleId(),
                    event.getBookingId());

            // Publish HoldSlotSucceededEvent
            HoldSlotSucceededEvent successEvent = HoldSlotSucceededEvent.builder()
                    .bookingId(event.getBookingId())
                    .scheduleHoldId(holdId)
                    .holdExpireAt(ZonedDateTime.now().plusMinutes(15)) // 15 minutes hold
                    .packageScheduleId(event.getPackageScheduleId())
                    .build();

            publishScheduleEvent("HoldSlotSucceededEvent", event.getBookingId(),
                    correlationId, successEvent);

            log.info("Slot hold succeeded: bookingId={}, holdId={}",
                    event.getBookingId(), holdId);

        } catch (Exception e) {
            // Extract bookingId from payload for error handling
            String bookingId = payloadNode.has("bookingId") ? payloadNode.get("bookingId").asText() : "unknown";

            log.error("Failed to hold slot for booking: {}", bookingId, e);

            // Publish HoldSlotFailedEvent
            HoldSlotFailedEvent failedEvent = HoldSlotFailedEvent.builder()
                    .bookingId(bookingId)
                    .reason(e.getMessage())
                    .build();

            publishScheduleEvent("HoldSlotFailedEvent", bookingId, correlationId, failedEvent);
        }
    }

    /**
     * Step 7: Handle BookingConfirmedEvent - Confirm hold to BOOKED
     */
    @KafkaListener(topics = "schedule-commands", groupId = "schedule-group")
    @Transactional
    public void handleScheduleCommands(String jsonMessage) {
        try {
            log.info("Received schedule command: {}", jsonMessage);

            // Handle potential double-serialization
            JsonNode rootNode = objectMapper.readTree(jsonMessage);
            if (rootNode.isTextual()) {
                rootNode = objectMapper.readTree(rootNode.asText());
            }

            // Convert to EventEnvelope
            EventEnvelope<?> envelope = objectMapper.treeToValue(rootNode, EventEnvelope.class);

            String eventType = envelope.getEventType();
            String aggregateId = envelope.getAggregateId();
            Object payload = envelope.getPayload();

            log.info("Schedule received command: type={}, aggregateId={}", eventType, aggregateId);

            if ("BookingConfirmedEvent".equals(eventType) && payload != null) {
                // Convert payload to JsonNode for easier handling
                var payloadNode = objectMapper.valueToTree(payload);
                handleBookingConfirmed(payloadNode);
            } else {
                log.warn("Unknown command type or missing payload: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error handling schedule command", e);
        }
    }

    private void handleBookingConfirmed(com.fasterxml.jackson.databind.JsonNode payloadNode) {
        try {
            BookingConfirmedEvent event = objectMapper.convertValue(payloadNode, BookingConfirmedEvent.class);

            log.info("Confirming hold: scheduleHoldId={}", event.getScheduleHoldId());

            // Confirm hold → status BOOKED
            scheduleService.confirmHoldScheduleForBooking(
                    event.getScheduleHoldId(),
                    event.getBookingId());

            log.info("Hold confirmed to BOOKED: holdId={}, bookingId={}",
                    event.getScheduleHoldId(), event.getBookingId());

        } catch (Exception e) {
            // Extract bookingId from payload for error handling
            String bookingId = payloadNode.has("bookingId") ? payloadNode.get("bookingId").asText() : "unknown";
            log.error("Failed to confirm hold for booking: {}", bookingId, e);
        }
    }

    private void publishScheduleEvent(String eventType, String aggregateId,
            String correlationId, Object payload) {
        try {
            EventEnvelope<Object> envelope = EventEnvelope.of(
                    eventType,
                    aggregateId,
                    correlationId,
                    "schedule-service",
                    payload);

            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send("schedule-events", aggregateId, json);

            log.info("Published {}: aggregateId={}", eventType, aggregateId);
        } catch (Exception e) {
            log.error("Failed to publish schedule event", e);
            throw new RuntimeException(e);
        }
    }
}