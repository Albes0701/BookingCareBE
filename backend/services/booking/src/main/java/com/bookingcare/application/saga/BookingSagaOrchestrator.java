package com.bookingcare.application.saga;

import com.bookingcare.application.dto.event.*;
import com.bookingcare.application.ports.output.IHealthCheckPackageScheduleBookingDetailRepository;
import com.bookingcare.domain.entity.HealthCheckPackageScheduleBookingDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingSagaOrchestrator {

    private final IHealthCheckPackageScheduleBookingDetailRepository bookingRepository;
    private final BookingEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * Step 3: Handle HoldSlotSucceededEvent from Schedule Service
     */
    @KafkaListener(topics = "schedule-events", groupId = "booking-saga-group")
    @Transactional
    public void handleScheduleEvents(String jsonMessage) {
        try {
            EventEnvelope<?> envelope = objectMapper.readValue(jsonMessage, EventEnvelope.class);
            
            log.info("Received event: type={}, aggregateId={}, correlationId={}", 
                    envelope.getEventType(), envelope.getAggregateId(), envelope.getCorrelationId());

            switch (envelope.getEventType()) {
                case "HoldSlotSucceededEvent":
                    handleHoldSlotSucceeded(envelope);
                    break;
                case "HoldSlotFailedEvent":
                    handleHoldSlotFailed(envelope);
                    break;
                default:
                    log.warn("Unknown event type: {}", envelope.getEventType());
            }
        } catch (Exception e) {
            log.error("Error handling schedule event", e);
        }
    }

    private void handleHoldSlotSucceeded(EventEnvelope<?> envelope) {
        try {
            // Parse payload
            HoldSlotSucceededEvent event = objectMapper.convertValue(
                    envelope.getPayload(), HoldSlotSucceededEvent.class);

            // Load booking
            HealthCheckPackageScheduleBookingDetail booking = bookingRepository
                    .findById(event.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + event.getBookingId()));

            // Update booking state
            booking.confirmHoldSchedule(event.getScheduleHoldId(), event.getHoldExpireAt());
            bookingRepository.save(booking);

            log.info("Booking hold confirmed: bookingId={}, scheduleHoldId={}", 
                    booking.getId(), event.getScheduleHoldId());

            // Step 4: Request payment
            PaymentRequestedEvent paymentEvent = PaymentRequestedEvent.builder()
                    .bookingId(booking.getId())
                    .patientId(booking.getPatientId())
                    .price(booking.getBookingPackageDetail().getPrice())
                    .description("Payment for booking " + booking.getId())
                    .build();

            eventPublisher.publishPaymentRequestedEvent(paymentEvent, envelope.getCorrelationId());

        } catch (Exception e) {
            log.error("Error handling HoldSlotSucceededEvent", e);
            throw new RuntimeException(e);
        }
    }

    private void handleHoldSlotFailed(EventEnvelope<?> envelope) {
        try {
            String bookingId = envelope.getAggregateId();
            
            HealthCheckPackageScheduleBookingDetail booking = bookingRepository
                    .findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

            booking.failHoldSchedule();
            bookingRepository.save(booking);

            log.warn("Booking failed due to slot unavailable: bookingId={}", bookingId);
        } catch (Exception e) {
            log.error("Error handling HoldSlotFailedEvent", e);
        }
    }

    /**
     * Step 5: Handle PaymentSucceededEvent from Payment Service
     */
    @KafkaListener(topics = "payment-events", groupId = "booking-saga-group")
    @Transactional
    public void handlePaymentEvents(String jsonMessage) {
        try {
            EventEnvelope<?> envelope = objectMapper.readValue(jsonMessage, EventEnvelope.class);
            
            log.info("Received payment event: type={}, aggregateId={}", 
                    envelope.getEventType(), envelope.getAggregateId());

            switch (envelope.getEventType()) {
                case "PaymentSucceededEvent":
                    handlePaymentSucceeded(envelope);
                    break;
                case "PaymentFailedEvent":
                    handlePaymentFailed(envelope);
                    break;
                default:
                    log.warn("Unknown payment event type: {}", envelope.getEventType());
            }
        } catch (Exception e) {
            log.error("Error handling payment event", e);
        }
    }

    private void handlePaymentSucceeded(EventEnvelope<?> envelope) {
        try {
            PaymentSucceededEvent event = objectMapper.convertValue(
                    envelope.getPayload(), PaymentSucceededEvent.class);

            // Load booking
            HealthCheckPackageScheduleBookingDetail booking = bookingRepository
                    .findById(event.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + event.getBookingId()));

            // Update booking: payment completed
            booking.confirmPayment();
            bookingRepository.save(booking);

            log.info("Payment confirmed: bookingId={}, paymentId={}", 
                    booking.getId(), event.getPaymentId());

            // Step 6: Confirm booking
            booking.confirmBooking();
            bookingRepository.save(booking);

            log.info("Booking confirmed: bookingId={}, status={}", 
                    booking.getId(), booking.getBookingStatus());

            // Step 7: Notify Schedule to confirm hold → BOOKED
            // Use scheduleHoldId from current saga state (stored during handleHoldSlotSucceeded)
            BookingConfirmedEvent confirmedEvent = BookingConfirmedEvent.builder()
                    .bookingId(booking.getId())
                    .scheduleHoldId(booking.getSagaState() != null ? booking.getSagaState().getScheduleHoldId() : "UNKNOWN")
                    .paymentId(event.getPaymentId())
                    .build();

            eventPublisher.publishBookingConfirmedEvent(confirmedEvent, envelope.getCorrelationId());

        } catch (Exception e) {
            log.error("Error handling PaymentSucceededEvent", e);
            throw new RuntimeException(e);
        }
    }

    private void handlePaymentFailed(EventEnvelope<?> envelope) {
        try {
            String bookingId = envelope.getAggregateId();
            
            HealthCheckPackageScheduleBookingDetail booking = bookingRepository
                    .findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

            booking.failPayment();
            bookingRepository.save(booking);

            log.warn("Booking payment failed: bookingId={}", bookingId);
            
        } catch (Exception e) {
            log.error("Error handling PaymentFailedEvent", e);
        }
    }
}