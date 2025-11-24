# SAGA HAPPY FLOW - CHI TIẾT LUỒNG DỮ LIỆU BOOKING

## Tổng Quan

Tài liệu này mô tả chi tiết **Happy Flow** của Saga Pattern trong hệ thống Booking sử dụng Kafka, bao gồm:
- Luồng dữ liệu qua từng class, method
- Kafka topics và events
- Trạng thái chuyển đổi của Booking entity

## Kiến Trúc

**Architectural Pattern:** Saga Orchestration Pattern (Booking Service làm Orchestrator)

**Messaging Infrastructure:** Apache Kafka

**Services:**
1. **Booking Service** - Orchestrator, quản lý saga state
2. **Schedule Service** - Quản lý slot giữ chỗ và xác nhận
3. **Payment Service** - Xử lý thanh toán

**Kafka Topics:**
- `booking-events` - Events từ Booking service
- `schedule-events` - Events từ Schedule service  
- `schedule-commands` - Commands gửi đến Schedule service
- `payment-commands` - Commands gửi đến Payment service
- `payment-events` - Events từ Payment service

---

## HAPPY FLOW - 7 BƯỚC

### **STEP 1: Client tạo booking**

#### 1.1. REST Endpoint
```
POST /api/booking/create
```

**Controller:**
```java
// File: d:\BookingCareBE\backend\services\booking\src\main\java\com\bookingcare\booking\container\controller\BookingController.java

@PostMapping("/create")
public ResponseEntity<String> createBooking(@RequestBody CreateBookingCommand cmd) {
    String bookingId = bookingApplicationService.createBooking(cmd);
    return ResponseEntity.ok(bookingId);
}
```

#### 1.2. Application Service
**Class:** `BookingApplicationService`  
**File:** `d:\BookingCareBE\backend\services\booking\src\main\java\com\bookingcare\booking\application\service\BookingApplicationService.java`  
**Method:** `createBooking(CreateBookingCommand cmd)`

**Logic:**
```java
public String createBooking(CreateBookingCommand cmd) {
    // 1. Map command to entity
    HealthCheckPackageScheduleBookingDetail booking = bookingMapper.toEntity(cmd);
    
    // 2. Initialize booking (set default values, generate IDs)
    booking.initialize();
    
    // 3. Save to database
    HealthCheckPackageScheduleBookingDetail savedBooking = 
        _healthCheckPackageScheduleBookingDetailRepository.save(booking);
    
    // 4. Generate correlation ID for distributed tracing
    String correlationId = java.util.UUID.randomUUID().toString();
    
    // 5. Create BookingCreatedEvent
    BookingCreatedEvent event = BookingCreatedEvent.builder()
        .bookingId(savedBooking.getId())
        .packageScheduleId(savedBooking.getPackageScheduleId())
        .patientId(savedBooking.getPatientId())
        .clinicId(savedBooking.getClinicId())
        .build();
    
    // 6. Publish event to Kafka
    eventPublisher.publishBookingCreatedEvent(event, correlationId);
    
    return savedBooking.getId();
}
```

**Domain Entity:**
```java
// File: d:\BookingCareBE\backend\services\booking\src\main\java\com\bookingcare\domain\entity\HealthCheckPackageScheduleBookingDetail.java

public void initialize() {
    this.bookingStatus = BookingStatus.PENDING;
    this.visitStatus = VisitStatus.PENDING;
    this.sagaStatus = SagaStatus.STARTED;
    this.currentSagaStep = SagaStep.BOOKING_CREATED;
    this.lastPaymentStatus = PaymentStatus.PENDING;
}
```

**Initial State:**
- `bookingStatus = PENDING`
- `sagaStatus = STARTED`
- `currentSagaStep = BOOKING_CREATED`

---

### **STEP 2: Publish BookingCreatedEvent**

**Class:** `BookingEventPublisher`  
**File:** `d:\BookingCareBE\backend\services\booking\src\main\java\com\bookingcare\booking\application\publisher\BookingEventPublisher.java`  
**Method:** `publishBookingCreatedEvent(BookingCreatedEvent event, String correlationId)`

**Logic:**
```java
public void publishBookingCreatedEvent(BookingCreatedEvent event, String correlationId) {
    try {
        // Wrap event in envelope
        EventEnvelope<BookingCreatedEvent> envelope = EventEnvelope.of(
            "BookingCreatedEvent",       // eventType
            event.getBookingId(),        // aggregateId
            correlationId,               // correlationId
            "booking-service",           // source
            event                        // payload
        );
        
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(envelope);
        
        // Send to Kafka topic: booking-events
        kafkaTemplate.send("booking-events", event.getBookingId(), json);
        
        log.info("Published BookingCreatedEvent: bookingId={}", event.getBookingId());
    } catch (Exception e) {
        log.error("Failed to publish BookingCreatedEvent", e);
        throw new RuntimeException(e);
    }
}
```

**Event Structure:**
```json
{
  "eventType": "BookingCreatedEvent",
  "aggregateId": "BKG_20250108_001",
  "correlationId": "a1b2c3d4-uuid",
  "timestamp": "2025-01-08T10:00:00Z",
  "source": "booking-service",
  "payload": {
    "bookingId": "BKG_20250108_001",
    "packageScheduleId": "PKGSCHDL_PKG01_SLOT001_20250115",
    "patientId": "PAT_123",
    "clinicId": "CLI_456"
  }
}
```

**Kafka Topic:** `booking-events`

---

### **STEP 3: Schedule Service nhận event và giữ chỗ**

#### 3.1. Kafka Listener
**Class:** `BookingEventListener`  
**File:** `d:\BookingCareBE\backend\services\schedule\src\main\java\com\bookingcare\infrastructure\messaging\listener\BookingEventListener.java`  
**Method:** `handleBookingEvents(String jsonMessage)`

**Logic:**
```java
@KafkaListener(topics = "booking-events", groupId = "schedule-group")
@Transactional
public void handleBookingEvents(String jsonMessage) {
    // 1. Deserialize JSON to EventEnvelope
    EventEnvelope<?> envelope = objectMapper.readValue(jsonMessage, EventEnvelope.class);
    
    log.info("Schedule received event: type={}, aggregateId={}", 
            envelope.getEventType(), envelope.getAggregateId());
    
    // 2. Route to handler based on event type
    if ("BookingCreatedEvent".equals(envelope.getEventType())) {
        handleBookingCreated(envelope);
    }
}

private void handleBookingCreated(EventEnvelope<?> envelope) {
    // 1. Parse payload
    BookingCreatedEvent event = objectMapper.convertValue(
            envelope.getPayload(), BookingCreatedEvent.class);
    
    log.info("Attempting to hold slot: bookingId={}, packageScheduleId={}", 
            event.getBookingId(), event.getPackageScheduleId());
    
    // 2. Call domain service to hold slot
    String holdId = scheduleService.holdScheduleForBooking(
            event.getPackageScheduleId(),
            event.getBookingId()
    );
    
    // 3. Create success event
    HoldSlotSucceededEvent successEvent = HoldSlotSucceededEvent.builder()
            .bookingId(event.getBookingId())
            .scheduleHoldId(holdId)
            .holdExpireAt(ZonedDateTime.now().plusMinutes(15)) // 15 minutes hold
            .packageScheduleId(event.getPackageScheduleId())
            .build();
    
    // 4. Publish to schedule-events topic
    publishScheduleEvent("HoldSlotSucceededEvent", event.getBookingId(), 
            envelope.getCorrelationId(), successEvent);
}
```

#### 3.2. Domain Service - Hold Schedule
**Class:** `ScheduleApplicationServicePatient`  
**File:** `d:\BookingCareBE\backend\services\schedule\src\main\java\com\bookingcare\application\handler\ScheduleApplicationServicePatient.java`  
**Method:** `holdScheduleForBooking(String packageScheduleId, String bookingId)`

**Logic:**
```java
@Transactional
public String holdScheduleForBooking(String packageScheduleId, String bookingId) {
    // 1. Tìm package schedule
    var packageSchedule = _healthCheckPackageSchedulesRepository
            .findById(packageScheduleId)
            .orElseThrow(() -> new RuntimeException("Package schedule not found: " + packageScheduleId));
    
    // 2. Kiểm tra capacity
    int availableSlots = packageSchedule.getCapacity() - packageSchedule.getBookedCount();
    if (availableSlots <= 0) {
        throw new RuntimeException("No available slots for package schedule: " + packageScheduleId);
    }
    
    // 3. Tạo ScheduleHold record
    String scheduleHoldId = generateScheduleHoldId(); // H + timestamp + UUID
    ScheduleHold scheduleHold = ScheduleHold.builder()
            .id(scheduleHoldId)
            .packageScheduleId(packageScheduleId)
            .bookingId(bookingId)
            .status("HOLD")
            .expireAt(ZonedDateTime.now().plusMinutes(15)) // 15 phút
            .createdAt(ZonedDateTime.now())
            .updatedAt(ZonedDateTime.now())
            .build();
    
    // 4. Lưu vào database
    _scheduleHoldRepository.save(scheduleHold);
    
    log.info("Hold schedule created: {} for booking: {}", scheduleHoldId, bookingId);
    return scheduleHoldId;
}

private String generateScheduleHoldId() {
    return "H" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
}
```

**Database Record Created:**
```sql
-- Table: schedule_holds
INSERT INTO schedule_holds (
    id,                      -- "H1704700800000_a1b2c3d4"
    package_schedule_id,     -- "PKGSCHDL_PKG01_SLOT001_20250115"
    booking_id,              -- "BKG_20250108_001"
    status,                  -- "HOLD"
    expire_at,               -- "2025-01-08T10:15:00Z"
    created_at,
    updated_at
)
```

#### 3.3. Publish HoldSlotSucceededEvent
**Method:** `publishScheduleEvent(String eventType, String aggregateId, String correlationId, Object payload)`

**Event Structure:**
```json
{
  "eventType": "HoldSlotSucceededEvent",
  "aggregateId": "BKG_20250108_001",
  "correlationId": "a1b2c3d4-uuid",
  "timestamp": "2025-01-08T10:00:05Z",
  "source": "schedule-service",
  "payload": {
    "bookingId": "BKG_20250108_001",
    "scheduleHoldId": "H1704700800000_a1b2c3d4",
    "holdExpireAt": "2025-01-08T10:15:00Z",
    "packageScheduleId": "PKGSCHDL_PKG01_SLOT001_20250115"
  }
}
```

**Kafka Topic:** `schedule-events`

---

### **STEP 4: Booking Saga Orchestrator xử lý hold success và request payment**

#### 4.1. Kafka Listener
**Class:** `BookingSagaOrchestrator`  
**File:** `d:\BookingCareBE\backend\services\booking\src\main\java\com\bookingcare\application\saga\BookingSagaOrchestrator.java`  
**Method:** `handleScheduleEvents(String jsonMessage)` → `handleHoldSlotSucceeded(EventEnvelope<?> envelope)`

**Logic:**
```java
@KafkaListener(topics = "schedule-events", groupId = "booking-saga-group")
@Transactional
public void handleScheduleEvents(String jsonMessage) {
    // 1. Deserialize envelope
    EventEnvelope<?> envelope = objectMapper.readValue(jsonMessage, EventEnvelope.class);
    
    log.info("Received event: type={}, aggregateId={}, correlationId={}", 
            envelope.getEventType(), envelope.getAggregateId(), envelope.getCorrelationId());
    
    // 2. Route to handler
    if ("HoldSlotSucceededEvent".equals(envelope.getEventType())) {
        handleHoldSlotSucceeded(envelope);
    }
}

private void handleHoldSlotSucceeded(EventEnvelope<?> envelope) {
    // 1. Parse event payload
    HoldSlotSucceededEvent event = objectMapper.convertValue(
            envelope.getPayload(), HoldSlotSucceededEvent.class);
    
    // 2. Load booking aggregate from database
    HealthCheckPackageScheduleBookingDetail booking = bookingRepository
            .findById(event.getBookingId())
            .orElseThrow(() -> new RuntimeException("Booking not found: " + event.getBookingId()));
    
    // 3. Update booking state: confirm hold schedule
    booking.confirmHoldSchedule(event.getScheduleHoldId(), event.getHoldExpireAt());
    bookingRepository.save(booking);
    
    log.info("Booking hold confirmed: bookingId={}, scheduleHoldId={}", 
            booking.getId(), event.getScheduleHoldId());
    
    // 4. Request payment - create PaymentRequestedEvent
    PaymentRequestedEvent paymentEvent = PaymentRequestedEvent.builder()
            .bookingId(booking.getId())
            .patientId(booking.getPatientId())
            .amount(booking.getBookingPackageDetail().getPrice())
            .description("Payment for booking " + booking.getId())
            .build();
    
    // 5. Publish to payment-commands topic
    eventPublisher.publishPaymentRequestedEvent(paymentEvent, envelope.getCorrelationId());
}
```

#### 4.2. Domain Entity - Update State
**Class:** `HealthCheckPackageScheduleBookingDetail`  
**Method:** `confirmHoldSchedule(String scheduleHoldId, ZonedDateTime holdExpireAt)`

**Logic:**
```java
public void confirmHoldSchedule(String scheduleHoldId, ZonedDateTime holdExpireAt) {
    this.scheduleHoldId = scheduleHoldId;
    this.holdExpireAt = holdExpireAt;
    this.bookingStatus = BookingStatus.PENDING_PAYMENT;
    this.currentSagaStep = SagaStep.SLOT_HELD;
    this.updatedAt = ZonedDateTime.now();
}
```

**State After Update:**
- `scheduleHoldId = "H1704700800000_a1b2c3d4"`
- `holdExpireAt = "2025-01-08T10:15:00Z"`
- `bookingStatus = PENDING_PAYMENT`
- `currentSagaStep = SLOT_HELD`

#### 4.3. Publish PaymentRequestedEvent
**Class:** `BookingEventPublisher`  
**Method:** `publishPaymentRequestedEvent(PaymentRequestedEvent event, String correlationId)`

**Event Structure:**
```json
{
  "eventType": "PaymentRequestedEvent",
  "aggregateId": "BKG_20250108_001",
  "correlationId": "a1b2c3d4-uuid",
  "timestamp": "2025-01-08T10:00:10Z",
  "source": "booking-service",
  "payload": {
    "bookingId": "BKG_20250108_001",
    "patientId": "PAT_123",
    "amount": 500000,
    "description": "Payment for booking BKG_20250108_001"
  }
}
```

**Kafka Topic:** `payment-commands`

---

### **STEP 5: Payment Service xử lý thanh toán**

#### 5.1. Kafka Listener
**Class:** `PaymentEventListener`  
**File:** `d:\BookingCareBE\backend\services\payment\src\main\java\com\bookingcare\payment\kafka\listener\PaymentEventListener.java`  
**Method:** `handlePaymentCommands(String jsonMessage)` → `handlePaymentRequested(EventEnvelope<?> envelope)`

**Logic:**
```java
@KafkaListener(topics = "payment-commands", groupId = "payment-group")
@Transactional
public void handlePaymentCommands(String jsonMessage) {
    // 1. Deserialize envelope
    EventEnvelope<?> envelope = objectMapper.readValue(jsonMessage, EventEnvelope.class);
    
    log.info("Payment received command: type={}, aggregateId={}", 
            envelope.getEventType(), envelope.getAggregateId());
    
    // 2. Route to handler
    if ("PaymentRequestedEvent".equals(envelope.getEventType())) {
        handlePaymentRequested(envelope);
    }
}

private void handlePaymentRequested(EventEnvelope<?> envelope) {
    // 1. Parse event payload
    PaymentRequestedEvent event = objectMapper.convertValue(
            envelope.getPayload(), PaymentRequestedEvent.class);
    
    log.info("Processing payment: bookingId={}, amount={}", 
            event.getBookingId(), event.getAmount());
    
    // 2. Create PaymentRequestCreate record
    PaymentRequestCreate paymentRequest = new PaymentRequestCreate(
            event.getBookingId(),
            event.getAmount().intValue(), // Convert BigDecimal to int
            event.getDescription()
    );
    
    // 3. Call existing payment service method
    PaymentResponseDTO paymentResponse = paymentService.CreatePaymentAsync(paymentRequest);
    
    log.info("Payment created: paymentId={}, bookingId={}", 
            paymentResponse.id(), event.getBookingId());
    
    // 4. Auto-publish success (in production: PayOS webhook triggers this)
    PaymentSucceededEvent successEvent = PaymentSucceededEvent.builder()
            .bookingId(event.getBookingId())
            .paymentId(paymentResponse.id())
            .transactionId("TXN_" + System.currentTimeMillis())
            .build();
    
    // 5. Publish to payment-events topic
    publishPaymentEvent("PaymentSucceededEvent", event.getBookingId(), 
            envelope.getCorrelationId(), successEvent);
}
```

#### 5.2. Payment Service - Create Payment
**Class:** `PaymentServiceIMP`  
**File:** `d:\BookingCareBE\backend\services\payment\src\main\java\com\bookingcare\payment\service\implementService\PaymentServiceIMP.java`  
**Method:** `CreatePaymentAsync(PaymentRequestCreate requestDTO)`

**Logic (existing implementation):**
```java
public PaymentResponseDTO CreatePaymentAsync(PaymentRequestCreate requestDTO) {
    // 1. Create payment record in database
    // 2. Call PayOS API to generate payment link
    // 3. Return PaymentResponseDTO with paymentId and checkoutUrl
    
    return new PaymentResponseDTO(
        generatedPaymentId,    // e.g., "PAY_1704700820000"
        checkoutUrl,
        qrCode,
        amount
    );
}
```

#### 5.3. Publish PaymentSucceededEvent
**Method:** `publishPaymentEvent(String eventType, String aggregateId, String correlationId, Object payload)`

**Event Structure:**
```json
{
  "eventType": "PaymentSucceededEvent",
  "aggregateId": "BKG_20250108_001",
  "correlationId": "a1b2c3d4-uuid",
  "timestamp": "2025-01-08T10:00:15Z",
  "source": "payment-service",
  "payload": {
    "bookingId": "BKG_20250108_001",
    "paymentId": "PAY_1704700820000",
    "transactionId": "TXN_1704700815000"
  }
}
```

**Kafka Topic:** `payment-events`

---

### **STEP 6: Booking Saga Orchestrator xác nhận booking**

#### 6.1. Kafka Listener
**Class:** `BookingSagaOrchestrator`  
**Method:** `handlePaymentEvents(String jsonMessage)` → `handlePaymentSucceeded(EventEnvelope<?> envelope)`

**Logic:**
```java
@KafkaListener(topics = "payment-events", groupId = "booking-saga-group")
@Transactional
public void handlePaymentEvents(String jsonMessage) {
    // 1. Deserialize envelope
    EventEnvelope<?> envelope = objectMapper.readValue(jsonMessage, EventEnvelope.class);
    
    log.info("Received payment event: type={}, aggregateId={}", 
            envelope.getEventType(), envelope.getAggregateId());
    
    // 2. Route to handler
    if ("PaymentSucceededEvent".equals(envelope.getEventType())) {
        handlePaymentSucceeded(envelope);
    }
}

private void handlePaymentSucceeded(EventEnvelope<?> envelope) {
    // 1. Parse event payload
    PaymentSucceededEvent event = objectMapper.convertValue(
            envelope.getPayload(), PaymentSucceededEvent.class);
    
    // 2. Load booking aggregate
    HealthCheckPackageScheduleBookingDetail booking = bookingRepository
            .findById(event.getBookingId())
            .orElseThrow(() -> new RuntimeException("Booking not found: " + event.getBookingId()));
    
    // 3. Update payment status
    booking.confirmPayment();
    booking.setExternalPaymentId(event.getPaymentId());
    booking.setLastPaymentStatus(PaymentStatus.COMPLETED);
    bookingRepository.save(booking);
    
    log.info("Payment confirmed: bookingId={}, paymentId={}", 
            booking.getId(), event.getPaymentId());
    
    // 4. Confirm booking
    booking.confirmBooking();
    bookingRepository.save(booking);
    
    log.info("Booking confirmed: bookingId={}, status={}", 
            booking.getId(), booking.getBookingStatus());
    
    // 5. Notify Schedule service to confirm hold → BOOKED
    BookingConfirmedEvent confirmedEvent = BookingConfirmedEvent.builder()
            .bookingId(booking.getId())
            .scheduleHoldId(booking.getScheduleHoldId())
            .paymentId(event.getPaymentId())
            .build();
    
    // 6. Publish to schedule-commands topic
    eventPublisher.publishBookingConfirmedEvent(confirmedEvent, envelope.getCorrelationId());
}
```

#### 6.2. Domain Entity - Update State
**Methods:**
1. `confirmPayment()`
2. `confirmBooking()`

**Logic:**
```java
public void confirmPayment() {
    this.lastPaymentStatus = PaymentStatus.COMPLETED;
    this.currentSagaStep = SagaStep.PAYMENT_COMPLETED;
    this.updatedAt = ZonedDateTime.now();
}

public void confirmBooking() {
    this.bookingStatus = BookingStatus.CONFIRMED;
    this.sagaStatus = SagaStatus.COMPLETED;
    this.currentSagaStep = SagaStep.BOOKING_CONFIRMED;
    this.updatedAt = ZonedDateTime.now();
}
```

**State After Update:**
- `externalPaymentId = "PAY_1704700820000"`
- `lastPaymentStatus = COMPLETED`
- `bookingStatus = CONFIRMED`
- `sagaStatus = COMPLETED`
- `currentSagaStep = BOOKING_CONFIRMED`

#### 6.3. Publish BookingConfirmedEvent
**Class:** `BookingEventPublisher`  
**Method:** `publishBookingConfirmedEvent(BookingConfirmedEvent event, String correlationId)`

**Event Structure:**
```json
{
  "eventType": "BookingConfirmedEvent",
  "aggregateId": "BKG_20250108_001",
  "correlationId": "a1b2c3d4-uuid",
  "timestamp": "2025-01-08T10:00:20Z",
  "source": "booking-service",
  "payload": {
    "bookingId": "BKG_20250108_001",
    "scheduleHoldId": "H1704700800000_a1b2c3d4",
    "paymentId": "PAY_1704700820000"
  }
}
```

**Kafka Topic:** `schedule-commands`

---

### **STEP 7: Schedule Service xác nhận hold thành BOOKED**

#### 7.1. Kafka Listener
**Class:** `BookingEventListener`  
**Method:** `handleScheduleCommands(String jsonMessage)` → `handleBookingConfirmed(EventEnvelope<?> envelope)`

**Logic:**
```java
@KafkaListener(topics = "schedule-commands", groupId = "schedule-group")
@Transactional
public void handleScheduleCommands(String jsonMessage) {
    // 1. Deserialize envelope
    EventEnvelope<?> envelope = objectMapper.readValue(jsonMessage, EventEnvelope.class);
    
    log.info("Schedule received command: type={}, aggregateId={}", 
            envelope.getEventType(), envelope.getAggregateId());
    
    // 2. Route to handler
    if ("BookingConfirmedEvent".equals(envelope.getEventType())) {
        handleBookingConfirmed(envelope);
    }
}

private void handleBookingConfirmed(EventEnvelope<?> envelope) {
    // 1. Parse event payload
    BookingConfirmedEvent event = objectMapper.convertValue(
            envelope.getPayload(), BookingConfirmedEvent.class);
    
    log.info("Confirming hold: scheduleHoldId={}", event.getScheduleHoldId());
    
    // 2. Call domain service to confirm hold
    scheduleService.confirmHoldScheduleForBooking(
            event.getScheduleHoldId(),
            event.getBookingId()
    );
    
    log.info("Hold confirmed to BOOKED: holdId={}, bookingId={}", 
            event.getScheduleHoldId(), event.getBookingId());
}
```

#### 7.2. Domain Service - Confirm Hold
**Class:** `ScheduleApplicationServicePatient`  
**Method:** `confirmHoldScheduleForBooking(String scheduleHoldId, String bookingId)`

**Logic:**
```java
@Transactional
public Boolean confirmHoldScheduleForBooking(String scheduleHoldId, String bookingId) {
    // 1. Tìm ScheduleHold record
    var scheduleHold = _scheduleHoldRepository
            .findById(scheduleHoldId)
            .orElseThrow(() -> new RuntimeException("Schedule hold not found: " + scheduleHoldId));
    
    // 2. Validate bookingId match
    if (!scheduleHold.getBookingId().equals(bookingId)) {
        throw new RuntimeException("BookingId mismatch for hold: " + scheduleHoldId);
    }
    
    // 3. Validate status is HOLD
    if (!"HOLD".equals(scheduleHold.getStatus())) {
        throw new RuntimeException("Hold status is not HOLD: " + scheduleHoldId);
    }
    
    // 4. Validate not expired
    if (ZonedDateTime.now().isAfter(scheduleHold.getExpireAt())) {
        throw new RuntimeException("Hold has expired: " + scheduleHoldId);
    }
    
    // 5. Update ScheduleHold status → BOOKED
    scheduleHold.setStatus("BOOKED");
    scheduleHold.setUpdatedAt(ZonedDateTime.now());
    _scheduleHoldRepository.save(scheduleHold);
    
    // 6. Update HealthCheckPackageSchedule bookedCount
    var packageSchedule = _healthCheckPackageSchedulesRepository
            .findById(scheduleHold.getPackageScheduleId())
            .orElseThrow(() -> new RuntimeException("Package schedule not found: " + scheduleHold.getPackageScheduleId()));
    
    packageSchedule.setBookedCount(packageSchedule.getBookedCount() + 1);
    packageSchedule.setUpdatedAt(ZonedDateTime.now());
    _healthCheckPackageSchedulesRepository.save(packageSchedule);
    
    log.info("Hold schedule confirmed: {} for booking: {}", scheduleHoldId, bookingId);
    return true;
}
```

**Database Updates:**
```sql
-- Update schedule_holds
UPDATE schedule_holds 
SET status = 'BOOKED', updated_at = NOW()
WHERE id = 'H1704700800000_a1b2c3d4';

-- Update health_check_package_schedules
UPDATE health_check_package_schedules
SET booked_count = booked_count + 1, updated_at = NOW()
WHERE id = 'PKGSCHDL_PKG01_SLOT001_20250115';
```

---

## TỔNG KẾT LUỒNG DỮ LIỆU

### Trạng Thái Booking Entity Qua Từng Bước

| Step | Saga Step | Booking Status | Payment Status | Saga Status |
|------|-----------|----------------|----------------|-------------|
| 1 | BOOKING_CREATED | PENDING | PENDING | STARTED |
| 4 | SLOT_HELD | PENDING_PAYMENT | PENDING | IN_PROGRESS |
| 6a | PAYMENT_COMPLETED | PENDING_PAYMENT | COMPLETED | IN_PROGRESS |
| 6b | BOOKING_CONFIRMED | CONFIRMED | COMPLETED | COMPLETED |

### Event Timeline

```
Time  Service    Event                      Topic              Handler
----  -------    -------------------------  -----------------  -------------------------------
T+0   Booking    BookingCreatedEvent        booking-events     Schedule.handleBookingCreated()
T+5   Schedule   HoldSlotSucceededEvent     schedule-events    Booking.handleHoldSlotSucceeded()
T+10  Booking    PaymentRequestedEvent      payment-commands   Payment.handlePaymentRequested()
T+15  Payment    PaymentSucceededEvent      payment-events     Booking.handlePaymentSucceeded()
T+20  Booking    BookingConfirmedEvent      schedule-commands  Schedule.handleBookingConfirmed()
```

### Database State Changes

**Booking Service:**
```
health_check_package_schedule_booking_detail:
  - id: "BKG_20250108_001"
  - booking_status: PENDING → PENDING_PAYMENT → CONFIRMED
  - schedule_hold_id: NULL → "H1704700800000_a1b2c3d4"
  - external_payment_id: NULL → "PAY_1704700820000"
  - last_payment_status: PENDING → COMPLETED
  - saga_status: STARTED → IN_PROGRESS → COMPLETED
  - current_saga_step: BOOKING_CREATED → SLOT_HELD → PAYMENT_COMPLETED → BOOKING_CONFIRMED
```

**Schedule Service:**
```
schedule_holds:
  - id: "H1704700800000_a1b2c3d4"
  - status: HOLD → BOOKED
  - expire_at: "2025-01-08T10:15:00Z"

health_check_package_schedules:
  - id: "PKGSCHDL_PKG01_SLOT001_20250115"
  - booked_count: 0 → 1
```

**Payment Service:**
```
payments:
  - id: "PAY_1704700820000"
  - booking_id: "BKG_20250108_001"
  - amount: 500000
  - status: "COMPLETED"
```

---

## KẾT LUẬN

**Happy Flow hoàn chỉnh** khi:
✅ Booking được tạo với status CONFIRMED  
✅ Schedule hold được confirm thành BOOKED  
✅ Payment được tạo và hoàn thành  
✅ Saga status = COMPLETED  
✅ bookedCount được increment  

**Không có:**
❌ Compensation logic (rollback khi fail)  
❌ Timeout handling  
❌ Outbox pattern implementation  
❌ Idempotent consumer (duplicate detection)  

Các tính năng trên sẽ được implement trong phase tiếp theo.
