# HƯỚNG DẪN HOÀN THÀNH PHƯƠNG ÁN 2

## 📋 Danh sách công việc cần hoàn thành

### 1️⃣ Cập nhật BookingSagaOrchestrator - handleHoldSlotSucceeded()

**File:** `d:\BookingCareBE\backend\services\booking\src\main\java\com\bookingcare\application\saga\BookingSagaOrchestrator.java`

**Hiện tại:**
```java
private void handleHoldSlotSucceeded(EventEnvelope<?> envelope) {
    try {
        HoldSlotSucceededEvent event = objectMapper.convertValue(
                envelope.getPayload(), HoldSlotSucceededEvent.class);

        HealthCheckPackageScheduleBookingDetail booking = bookingRepository
                .findById(event.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found: " + event.getBookingId()));

        booking.confirmHoldSchedule(event.getScheduleHoldId(), event.getHoldExpireAt());
        bookingRepository.save(booking);

        log.info("Booking hold confirmed: bookingId={}, scheduleHoldId={}", 
                booking.getId(), event.getScheduleHoldId());

        // Step 4: Request payment
        PaymentRequestedEvent paymentEvent = PaymentRequestedEvent.builder()
                .bookingId(booking.getId())
                .patientId(booking.getPatientId())
                .amount(booking.getBookingPackageDetail().getPrice())
                .description("Payment for booking " + booking.getId())
                .build();

        eventPublisher.publishPaymentRequestedEvent(paymentEvent, envelope.getCorrelationId());

    } catch (Exception e) {
        log.error("Error handling HoldSlotSucceededEvent", e);
        throw new RuntimeException(e);
    }
}
```

**Cần thêm:** Inject repository và cập nhật saga state
```java
// Thêm field vào class
private final IBooksingSagaStateRepository bookingSagaStateRepository;

// Cập nhật method:
private void handleHoldSlotSucceeded(EventEnvelope<?> envelope) {
    try {
        HoldSlotSucceededEvent event = objectMapper.convertValue(
                envelope.getPayload(), HoldSlotSucceededEvent.class);

        HealthCheckPackageScheduleBookingDetail booking = bookingRepository
                .findById(event.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found: " + event.getBookingId()));

        // Update booking status
        booking.confirmHoldSchedule(event.getScheduleHoldId(), event.getHoldExpireAt());
        bookingRepository.save(booking);

        // Update saga state with schedule hold info
        BookingSagaState sagaState = bookingSagaStateRepository
                .findByBookingId(event.getBookingId())
                .orElseThrow(() -> new RuntimeException("Saga state not found: " + event.getBookingId()));
        
        sagaState.confirmHoldSchedule(event.getScheduleHoldId(), event.getHoldExpireAt());
        bookingSagaStateRepository.save(sagaState);

        log.info("Booking hold confirmed: bookingId={}, scheduleHoldId={}, sagaStatus={}", 
                booking.getId(), event.getScheduleHoldId(), sagaState.getSagaStatus());

        // Step 4: Request payment
        PaymentRequestedEvent paymentEvent = PaymentRequestedEvent.builder()
                .bookingId(booking.getId())
                .patientId(booking.getPatientId())
                .amount(booking.getBookingPackageDetail().getPrice())
                .description("Payment for booking " + booking.getId())
                .build();

        eventPublisher.publishPaymentRequestedEvent(paymentEvent, envelope.getCorrelationId());

    } catch (Exception e) {
        log.error("Error handling HoldSlotSucceededEvent", e);
        throw new RuntimeException(e);
    }
}
```

---

### 2️⃣ Cập nhật BookingSagaOrchestrator - handlePaymentSucceeded()

**Cần thêm:** Cập nhật saga state với payment info

```java
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

        // Update saga state with payment info
        BookingSagaState sagaState = bookingSagaStateRepository
                .findByBookingId(event.getBookingId())
                .orElseThrow(() -> new RuntimeException("Saga state not found: " + event.getBookingId()));
        
        sagaState.confirmPayment(event.getPaymentId());  // Sets status to COMPLETED, step to PAYMENT_COMPLETED
        bookingSagaStateRepository.save(sagaState);

        log.info("Payment confirmed: bookingId={}, paymentId={}, sagaStatus={}", 
                booking.getId(), event.getPaymentId(), sagaState.getSagaStatus());

        // Step 6: Confirm booking
        booking.confirmBooking();
        bookingRepository.save(booking);

        // Update saga to COMPLETED
        sagaState.completeBooking();
        bookingSagaStateRepository.save(sagaState);

        log.info("Booking confirmed: bookingId={}, status={}, sagaStatus=COMPLETED", 
                booking.getId(), booking.getBookingStatus());

        // Step 7: Notify Schedule to confirm hold → BOOKED
        // Use scheduleHoldId from saga state
        BookingConfirmedEvent confirmedEvent = BookingConfirmedEvent.builder()
                .bookingId(booking.getId())
                .scheduleHoldId(sagaState.getScheduleHoldId())
                .paymentId(event.getPaymentId())
                .build();

        eventPublisher.publishBookingConfirmedEvent(confirmedEvent, envelope.getCorrelationId());

    } catch (Exception e) {
        log.error("Error handling PaymentSucceededEvent", e);
        throw new RuntimeException(e);
    }
}
```

---

### 3️⃣ Cập nhật BookingSagaOrchestrator - handleHoldSlotFailed()

```java
private void handleHoldSlotFailed(EventEnvelope<?> envelope) {
    try {
        String bookingId = envelope.getAggregateId();
        
        HealthCheckPackageScheduleBookingDetail booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        booking.failHoldSchedule();
        bookingRepository.save(booking);

        // Update saga state
        BookingSagaState sagaState = bookingSagaStateRepository
                .findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Saga state not found: " + bookingId));
        
        sagaState.failHoldSchedule();  // Sets status to FAILED
        bookingSagaStateRepository.save(sagaState);

        log.warn("Booking failed due to slot unavailable: bookingId={}, sagaStatus=FAILED", bookingId);
    } catch (Exception e) {
        log.error("Error handling HoldSlotFailedEvent", e);
    }
}
```

---

### 4️⃣ Cập nhật BookingSagaOrchestrator - handlePaymentFailed()

```java
private void handlePaymentFailed(EventEnvelope<?> envelope) {
    try {
        String bookingId = envelope.getAggregateId();
        
        HealthCheckPackageScheduleBookingDetail booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        booking.failPayment();
        bookingRepository.save(booking);

        // Update saga state
        BookingSagaState sagaState = bookingSagaStateRepository
                .findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Saga state not found: " + bookingId));
        
        sagaState.failPayment();  // Sets status to FAILED, step to FAILED
        bookingSagaStateRepository.save(sagaState);

        log.warn("Booking payment failed: bookingId={}, sagaStatus=FAILED", bookingId);
        
        // TODO: Publish compensation event to release hold slot
        // This will be implemented in Phase 2 (Compensation Logic)
    } catch (Exception e) {
        log.error("Error handling PaymentFailedEvent", e);
    }
}
```

---

### 5️⃣ Cập nhật BookingApplicationService - Uncomment saga state creation

**Hiện tại:** Code đã chuẩn bị nhưng cần inject repository

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingApplicationService implements IBookingApplicationService {
    // ... other dependencies ...
    private final BookingEventPublisher eventPublisher;
    // Thêm dòng này khi ready:
    // private final IBooksingSagaStateRepository bookingSagaStateRepository;
}
```

**Khi ready, uncomment code trong createBooking():**
```java
@Override
@Transactional
public String createBooking(CreateBookingCommand cmd) {
    // 1. Create and save booking
    HealthCheckPackageScheduleBookingDetail booking = bookingMapper.toEntity(cmd);
    booking.initialize();
    HealthCheckPackageScheduleBookingDetail savedBooking = _healthCheckPackageScheduleBookingDetailRepository.save(booking);

    // 2. Generate correlation ID for distributed tracing
    String correlationId = java.util.UUID.randomUUID().toString();
    
    // 3. Create and save saga state (UNCOMMENT khi ready)
    /*
    BookingSagaState sagaState = BookingSagaState.builder()
            .bookingId(savedBooking.getId())
            .correlationId(correlationId)
            .build();
    sagaState.initialize(savedBooking.getId(), correlationId);
    bookingSagaStateRepository.save(sagaState);
    */

    // 4. Publish BookingCreatedEvent
    BookingCreatedEvent event = BookingCreatedEvent.builder()
            .bookingId(savedBooking.getId())
            .packageScheduleId(savedBooking.getPackageScheduleId())
            .patientId(savedBooking.getPatientId())
            .clinicId(savedBooking.getClinicId())
            .build();

    eventPublisher.publishBookingCreatedEvent(event, correlationId);

    return savedBooking.getId();
}
```

---

## 📊 Validation Checklist

After implementing, verify:

- [ ] `BookingSagaState` entity compiles without errors
- [ ] `IBooksingSagaStateRepository` interface created
- [ ] Migration SQL file created at correct location
- [ ] `HealthCheckPackageScheduleBookingDetail` entity clean (no saga fields)
- [ ] `BookingSagaOrchestrator` updated with all handlers
- [ ] All imports resolved
- [ ] No compilation errors in all 3 services

---

## 🔍 Testing Suggestions

### Unit Test for BookingSagaState

```java
@Test
public void testBookingSagaStateTransitions() {
    BookingSagaState sagaState = BookingSagaState.builder()
            .bookingId("BKG_123")
            .correlationId("CORR_456")
            .build();
    
    sagaState.initialize("BKG_123", "CORR_456");
    assertEquals(SagaStatus.INITIATED, sagaState.getSagaStatus());
    assertEquals(SagaStep.BOOKING_CREATED, sagaState.getCurrentSagaStep());
    
    sagaState.confirmHoldSchedule("HOLD_789", ZonedDateTime.now());
    assertEquals(SagaStatus.IN_PROGRESS, sagaState.getSagaStatus());
    assertEquals(SagaStep.SLOT_HELD, sagaState.getCurrentSagaStep());
    
    sagaState.confirmPayment("PAY_999");
    assertEquals(PaymentStatus.COMPLETED, sagaState.getLastPaymentStatus());
    assertEquals(SagaStep.PAYMENT_COMPLETED, sagaState.getCurrentSagaStep());
    
    sagaState.completeBooking();
    assertEquals(SagaStatus.COMPLETED, sagaState.getSagaStatus());
    assertEquals(SagaStep.BOOKING_CONFIRMED, sagaState.getCurrentSagaStep());
}
```

---

## 📝 Documentation Location

- Architecture: `d:\BookingCareBE\backend\docs\PHƯƠNG_ÁN_2_SAGA_STATE_IMPLEMENTATION.md`
- Happy Flow: `d:\BookingCareBE\backend\docs\SAGA_HAPPY_FLOW_DOCUMENTATION.md`

---

**Status:** Ready for full implementation. All structural pieces in place.
