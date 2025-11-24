package com.bookingcare.infrastructure.messaging.listener;

import com.bookingcare.application.dto.event.BookingConfirmedEvent;
import com.bookingcare.application.dto.event.BookingCreatedEvent;
import com.bookingcare.application.dto.event.EventEnvelope;
import com.bookingcare.application.ports.input.IScheduleApplicationServicePatient;
import com.bookingcare.infrastructure.messaging.event.HoldSlotFailedEvent;
import com.bookingcare.infrastructure.messaging.event.HoldSlotSucceededEvent;
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
            EventEnvelope<?> envelope = objectMapper.readValue(jsonMessage, EventEnvelope.class);
            
            log.info("Schedule received event: type={}, aggregateId={}", 
                    envelope.getEventType(), envelope.getAggregateId());

            if ("BookingCreatedEvent".equals(envelope.getEventType())) {
                handleBookingCreated(envelope);
            }
        } catch (Exception e) {
            log.error("Error handling booking event", e);
        }
    }

    private void handleBookingCreated(EventEnvelope<?> envelope) {
        try {
            BookingCreatedEvent event = objectMapper.convertValue(
                    envelope.getPayload(), BookingCreatedEvent.class);

            log.info("Attempting to hold slot: bookingId={}, packageScheduleId={}", 
                    event.getBookingId(), event.getPackageScheduleId());

            // Call domain service to hold slot - returns holdId (String)
            String holdId = scheduleService.holdScheduleForBooking(
                    event.getPackageScheduleId(),
                    event.getBookingId()
            );

            // Publish HoldSlotSucceededEvent
            HoldSlotSucceededEvent successEvent = HoldSlotSucceededEvent.builder()
                    .bookingId(event.getBookingId())
                    .scheduleHoldId(holdId)
                    .holdExpireAt(ZonedDateTime.now().plusMinutes(15)) // 15 minutes hold
                    .packageScheduleId(event.getPackageScheduleId())
                    .build();

            publishScheduleEvent("HoldSlotSucceededEvent", event.getBookingId(), 
                    envelope.getCorrelationId(), successEvent);

            log.info("Slot hold succeeded: bookingId={}, holdId={}", 
                    event.getBookingId(), holdId);

        } catch (Exception e) {
            log.error("Failed to hold slot for booking: {}", envelope.getAggregateId(), e);
            
            // Publish HoldSlotFailedEvent
            HoldSlotFailedEvent failedEvent = HoldSlotFailedEvent.builder()
                    .bookingId(envelope.getAggregateId())
                    .reason(e.getMessage())
                    .build();

            publishScheduleEvent("HoldSlotFailedEvent", envelope.getAggregateId(), 
                    envelope.getCorrelationId(), failedEvent);
        }
    }

    /**
     * Step 7: Handle BookingConfirmedEvent - Confirm hold to BOOKED
     */
    @KafkaListener(topics = "schedule-commands", groupId = "schedule-group")
    @Transactional
    public void handleScheduleCommands(String jsonMessage) {
        try {
            EventEnvelope<?> envelope = objectMapper.readValue(jsonMessage, EventEnvelope.class);
            
            log.info("Schedule received command: type={}, aggregateId={}", 
                    envelope.getEventType(), envelope.getAggregateId());

            if ("BookingConfirmedEvent".equals(envelope.getEventType())) {
                handleBookingConfirmed(envelope);
            }
        } catch (Exception e) {
            log.error("Error handling schedule command", e);
        }
    }

    private void handleBookingConfirmed(EventEnvelope<?> envelope) {
        try {
            BookingConfirmedEvent event = objectMapper.convertValue(
                    envelope.getPayload(), BookingConfirmedEvent.class);

            log.info("Confirming hold: scheduleHoldId={}", event.getScheduleHoldId());

            // Confirm hold → status BOOKED
            scheduleService.confirmHoldScheduleForBooking(
                    event.getScheduleHoldId(),
                    event.getBookingId()
            );

            log.info("Hold confirmed to BOOKED: holdId={}, bookingId={}", 
                    event.getScheduleHoldId(), event.getBookingId());

        } catch (Exception e) {
            log.error("Failed to confirm hold: {}", envelope.getAggregateId(), e);
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
                    payload
            );

            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send("schedule-events", aggregateId, json);
            
            log.info("Published {}: aggregateId={}", eventType, aggregateId);
        } catch (Exception e) {
            log.error("Failed to publish schedule event", e);
            throw new RuntimeException(e);
        }
    }
}