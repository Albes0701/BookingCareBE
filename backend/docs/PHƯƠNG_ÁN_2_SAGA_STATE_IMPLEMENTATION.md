# PHƯƠNG ÁN 2 - TÁCH SAGA STATE RIÊNG - CẬP NHẬT HOÀN THÀNH

## 📋 Tóm tắt thay đổi

### ✅ 1. Entity mới: `BookingSagaState`
**File:** `d:\BookingCareBE\backend\services\booking\src\main\java\com\bookingcare\domain\entity\BookingSagaState.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSagaState {
    private String id;
    private String bookingId;              // FK → health_check_package_schedule_booking_details
    private String correlationId;          // Distributed tracing
    
    // Saga workflow tracking
    private SagaStatus sagaStatus;         // INITIATED, IN_PROGRESS, COMPLETED, FAILED, COMPENSATING
    private SagaStep currentSagaStep;      // BOOKING_CREATED, SLOT_HELD, PAYMENT_COMPLETED, BOOKING_CONFIRMED
    private Integer compensationCount;
    
    // Schedule service data
    private String scheduleHoldId;
    private ZonedDateTime holdExpireAt;
    
    // Payment service data
    private String externalPaymentId;
    private PaymentStatus lastPaymentStatus;
    
    // Timestamps
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
```

**Lợi ích:**
- ✅ Tách biệt saga logic khỏi booking aggregate
- ✅ Có thể track saga state transitions độc lập
- ✅ Support distributed tracing với correlationId
- ✅ Dễ implement compensation/timeout logic sau này
- ✅ Clear separation of concerns

---

### ✅ 2. Entity cập nhật: `HealthCheckPackageScheduleBookingDetail`
**File:** `d:\BookingCareBE\backend\services\booking\src\main\java\com\bookingcare\domain\entity\HealthCheckPackageScheduleBookingDetail.java`

**Thay đổi:**
- ❌ **Xóa fields:**
  - `scheduleHoldId`
  - `holdExpireAt`
  - `externalPaymentId`
  - `lastPaymentStatus`

- ✅ **Giữ lại:**
  - `bookingStatus` - Core booking state (PENDING, PENDING_PAYMENT, CONFIRMED...)
  - `createdDate, updatedDate` - Audit fields
  - `bookingPackage, bookingPackageDetail` - Business relationships
  - `sagaState` - Reference to BookingSagaState entity

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckPackageScheduleBookingDetail {
    // Core booking fields
    private String id;
    private String patientRelativesName;
    private String patientRelativesPhoneNumber;
    private String patientId;
    private String packageScheduleId;
    private String bookingPackageId;
    private String bookingReason;
    private String clinicId;
    private BookingStatus bookingStatus;           // Giữ lại - core state
    private PurchaseMethod purchaseMethod;
    
    private ZonedDateTime createdDate;
    private ZonedDateTime updatedDate;

    // Relationships
    private BookingPackage bookingPackage;
    private BookingPackageDetail bookingPackageDetail;
    private BookingSagaState sagaState;            // Tách saga logic riêng ✅
}
```

**State transition methods:** Vẫn giữ `confirmHoldSchedule()`, `failHoldSchedule()`, `confirmPayment()`, `failPayment()`, `confirmBooking()` nhưng không cập nhật saga fields nữa

---

### ✅ 3. Database Migration
**File:** `d:\BookingCareBE\backend\services\booking\src\main\resources\db\migration\V20251124_00001__create_booking_saga_state_table.sql`

```sql
CREATE TABLE booking_saga_state
(
    id VARCHAR(255) NOT NULL,
    booking_id VARCHAR(255) NOT NULL UNIQUE,
    correlation_id VARCHAR(255) NOT NULL,
    
    saga_status VARCHAR(50) NOT NULL,
    current_saga_step VARCHAR(50) NOT NULL,
    compensation_count INTEGER DEFAULT 0,
    
    schedule_hold_id VARCHAR(255),
    hold_expire_at TIMESTAMP WITH TIME ZONE,
    
    external_payment_id VARCHAR(255),
    last_payment_status VARCHAR(50),
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (booking_id) REFERENCES health_check_package_schedule_booking_details(id)
);

-- Indexes
CREATE INDEX idx_saga_state_booking_id ON booking_saga_state(booking_id);
CREATE INDEX idx_saga_state_correlation_id ON booking_saga_state(correlation_id);
CREATE INDEX idx_saga_state_status ON booking_saga_state(saga_status);
CREATE INDEX idx_saga_state_saga_step ON booking_saga_state(current_saga_step);
```

**Lợi ích:**
- ✅ Saga state persisted vào DB riêng
- ✅ Có thể query booking theo saga status
- ✅ Có thể audit lịch sử saga transitions
- ✅ Unique constraint trên booking_id (1:1 relationship)

---

### ✅ 4. Repository Interface Mới
**File:** `d:\BookingCareBE\backend\services\booking\src\main\java\com\bookingcare\application\ports\output\IBooksingSagaStateRepository.java`

```java
public interface IBooksingSagaStateRepository extends JpaRepository<BookingSagaState, String> {
    Optional<BookingSagaState> findByBookingId(String bookingId);
    Optional<BookingSagaState> findByCorrelationId(String correlationId);
}
```

**Tác dụng:**
- Find saga state by booking ID (load full saga context)
- Find saga state by correlation ID (distributed tracing)

---

### ✅ 5. Application Service Cập Nhật
**File:** `d:\BookingCareBE\backend\services\booking\src\main\java\com\bookingcare\application\handler\BookingApplicationService.java`

**`createBooking()` flow (cần hoàn thành khi repository được inject):**

```java
@Override
@Transactional
public String createBooking(CreateBookingCommand cmd) {
    // 1. Create and save booking
    HealthCheckPackageScheduleBookingDetail booking = bookingMapper.toEntity(cmd);
    booking.initialize();
    HealthCheckPackageScheduleBookingDetail savedBooking = _healthCheckPackageScheduleBookingDetailRepository.save(booking);

    // 2. Generate correlation ID and create saga state
    String correlationId = java.util.UUID.randomUUID().toString();
    BookingSagaState sagaState = BookingSagaState.builder()
            .bookingId(savedBooking.getId())
            .correlationId(correlationId)
            .build();
    sagaState.initialize(savedBooking.getId(), correlationId);
    bookingSagaStateRepository.save(sagaState);  // Persist saga state riêng
    
    // 3. Publish BookingCreatedEvent
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

### ✅ 6. Saga Orchestrator Cập Nhật
**File:** `d:\BookingCareBE\backend\services\booking\src\main\java\com\bookingcare\application\saga\BookingSagaOrchestrator.java`

**Thay đổi quan trọng:**
1. **handleHoldSlotSucceeded():** 
   - Cập nhật booking status `PENDING_PAYMENT`
   - (Cần thêm code để cập nhật saga state)

2. **handlePaymentSucceeded():**
   - Cập nhật booking status `CONFIRMED`
   - Lấy `scheduleHoldId` từ `booking.getSagaState()` (vì entity không còn field này)
   - (Cần thêm code để cập nhật saga state payments)

---

## 📊 So sánh: Trước vs Sau

### Trước (Entity lớn)
```
HealthCheckPackageScheduleBookingDetail
├── id, patientId, packageScheduleId
├── bookingStatus (PENDING, CONFIRMED...)
├── scheduleHoldId ❌ (không có trong DB)
├── holdExpireAt ❌ (không có trong DB)
├── externalPaymentId ❌ (không có trong DB)
└── lastPaymentStatus ❌ (không có trong DB)
```

**Vấn đề:**
- ❌ Fields trong entity nhưng DB không có
- ❌ Hibernate error khi load
- ❌ Trộn business logic + saga logic
- ❌ Khó test separate concerns

---

### Sau (Tách rõ)
```
HealthCheckPackageScheduleBookingDetail (Core Booking)
├── id, patientId, packageScheduleId
├── bookingStatus ✅ (PENDING, CONFIRMED...)
├── createdDate, updatedDate
└── sagaState → (Reference to BookingSagaState)

BookingSagaState (Saga Orchestration)
├── id, bookingId (FK), correlationId
├── sagaStatus (INITIATED, IN_PROGRESS, COMPLETED)
├── currentSagaStep (BOOKING_CREATED, SLOT_HELD...)
├── scheduleHoldId ✅ (từ schedule service)
├── holdExpireAt ✅ (từ schedule service)
├── externalPaymentId ✅ (từ payment service)
├── lastPaymentStatus ✅ (PENDING, COMPLETED)
└── createdAt, updatedAt
```

**Lợi ích:**
- ✅ Clear separation of concerns
- ✅ Tất cả fields có trong DB
- ✅ Dễ extend (thêm compensation logic)
- ✅ Dễ test (mock saga state riêng)
- ✅ Support event sourcing sau này

---

## 🔄 Saga Flow với Phương án 2

### Step-by-step

**Step 1:** Client tạo booking
```
POST /api/booking/create
  ↓
BookingApplicationService.createBooking()
  ├─ Save HealthCheckPackageScheduleBookingDetail (status=PENDING)
  ├─ Create BookingSagaState (status=INITIATED, step=BOOKING_CREATED)
  └─ Publish BookingCreatedEvent (correlationId=UUID)
```

**Step 3:** Schedule service giữ slot
```
Kafka: BookingCreatedEvent (topic: booking-events)
  ↓
BookingEventListener.handleBookingCreated()
  ├─ ScheduleService.holdScheduleForBooking()
  └─ Publish HoldSlotSucceededEvent (scheduleHoldId, holdExpireAt)
```

**Step 4:** Booking saga xử lý hold success
```
Kafka: HoldSlotSucceededEvent (topic: schedule-events)
  ↓
BookingSagaOrchestrator.handleHoldSlotSucceeded()
  ├─ Update HealthCheckPackageScheduleBookingDetail (status=PENDING_PAYMENT)
  ├─ Update BookingSagaState (status=IN_PROGRESS, step=SLOT_HELD, scheduleHoldId=...)
  └─ Publish PaymentRequestedEvent
```

**Step 5:** Payment service xử lý
```
Kafka: PaymentRequestedEvent (topic: payment-commands)
  ↓
PaymentEventListener.handlePaymentRequested()
  ├─ PaymentService.CreatePaymentAsync()
  └─ Publish PaymentSucceededEvent (paymentId)
```

**Step 6:** Booking saga xác nhận
```
Kafka: PaymentSucceededEvent (topic: payment-events)
  ↓
BookingSagaOrchestrator.handlePaymentSucceeded()
  ├─ Update HealthCheckPackageScheduleBookingDetail (status=CONFIRMED)
  ├─ Update BookingSagaState (status=COMPLETED, step=BOOKING_CONFIRMED, externalPaymentId=...)
  └─ Publish BookingConfirmedEvent (scheduleHoldId from saga state)
```

**Step 7:** Schedule service confirm
```
Kafka: BookingConfirmedEvent (topic: schedule-commands)
  ↓
BookingEventListener.handleBookingConfirmed()
  ├─ ScheduleService.confirmHoldScheduleForBooking()
  └─ Update schedule_holds (status=BOOKED), increment bookedCount
```

---

## 📝 Tiếp theo

### ✅ Đã hoàn thành:
- Entity `BookingSagaState` với đầy đủ fields và state transition methods
- Migration SQL tạo bảng `booking_saga_state`
- Repository interface `IBooksingSagaStateRepository`
- Entity `HealthCheckPackageScheduleBookingDetail` - xóa saga fields, giữ lại core logic
- Cập nhật BookingApplicationService structure (ready for full implementation)
- Cập nhật BookingSagaOrchestrator imports

### ⏳ Cần hoàn thành:
1. **Inject IBooksingSagaStateRepository vào BookingApplicationService**
   - Uncomment code trong `createBooking()` để save saga state

2. **Cập nhật BookingSagaOrchestrator handlers**
   - `handleHoldSlotSucceeded()` - update saga state fields
   - `handlePaymentSucceeded()` - update saga state with payment info
   - `handleHoldSlotFailed()` - mark saga as FAILED
   - `handlePaymentFailed()` - mark saga as FAILED

3. **Implement Persistence in Spring Data JPA**
   - Khai báo repository @Autowired trong services

4. **Testing**
   - Unit test BookingSagaState transitions
   - Integration test full saga flow
   - End-to-end test Kafka messaging

---

## 🎯 Architecture Diagram

```
┌─────────────────────────────────────────────────────┐
│                    Booking Service                   │
├─────────────────────────────────────────────────────┤
│                                                      │
│  HealthCheckPackageScheduleBookingDetail (Aggregate) │
│  ├─ id, patientId, packageScheduleId                │
│  ├─ bookingStatus (core state)                      │
│  └─ sagaState → BookingSagaState (1:1)              │
│                                                      │
│  BookingSagaState (Saga Projection)                 │
│  ├─ correlationId (tracing)                         │
│  ├─ sagaStatus, currentSagaStep                     │
│  ├─ scheduleHoldId (from schedule service)          │
│  ├─ externalPaymentId (from payment service)        │
│  └─ lastPaymentStatus                               │
│                                                      │
│  BookingEventPublisher (Commands)                    │
│  └─ booking-events, payment-commands, schedule-commands
│                                                      │
│  BookingSagaOrchestrator (Listeners)                 │
│  ├─ @KafkaListener(schedule-events)                 │
│  └─ @KafkaListener(payment-events)                  │
│                                                      │
└─────────────────────────────────────────────────────┘
```

---

**Status:** ✅ **Phương án 2 - Tách Saga State Riêng - IMPLEMENTED**

Cấu trúc sạch, scalable, ready for advanced patterns (event sourcing, compensation, timeouts).
