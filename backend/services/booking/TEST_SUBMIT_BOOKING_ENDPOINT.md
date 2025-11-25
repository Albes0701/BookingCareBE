# 🧪 HƯỚNG DẪN TEST ENDPOINT SUBMIT BOOKING

## 📌 Thông tin Endpoint

**URL:** `POST /api/v1/booking/submit-booking`  
**Service:** Booking Service (Port 8083)  
**Gateway:** http://localhost:8222  
**Full URL:** `http://localhost:8222/api/v1/booking/submit-booking`

---

## 🚀 BƯỚC 1: CHUẨN BỊ MÔI TRƯỜNG

### 1.1 Khởi động Docker Compose

```powershell
cd D:\BookingCareBE\backend
docker compose up -d
```

### 1.2 Kiểm tra Services đang chạy

```powershell
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

**Cần có các services:**
- ✅ `bookingcare_booking-service` (8083)
- ✅ `bookingcare_schedule-service` (8085)
- ✅ `bookingcare_payment-service` (8086)
- ✅ `bookingcare_gateway` (8222)
- ✅ `booking-kafka` (9092, 29092)
- ✅ `bookingcare_postgresql` (5432)

### 1.3 Kiểm tra Kafka Topics

```powershell
docker exec booking-kafka kafka-topics --list --bootstrap-server localhost:9092
```

**Expected Output:**
```
booking-events
schedule-events
schedule-commands
payment-events
payment-commands
```

Nếu thiếu topics, chạy lệnh tạo:
```powershell
# Tạo tất cả topics cần thiết
docker exec booking-kafka kafka-topics --create --topic booking-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
docker exec booking-kafka kafka-topics --create --topic schedule-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
docker exec booking-kafka kafka-topics --create --topic schedule-commands --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
docker exec booking-kafka kafka-topics --create --topic payment-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

---

## 🔐 BƯỚC 2: LẤY ACCESS TOKEN

### 2.1 Login để lấy Token

**Postman Request:**
```http
POST http://localhost:8222/api/v1/auth/sign-in
Content-Type: application/json

{
  "username": "admin@bookingcare.com",
  "password": "admin123"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBib29raW5nY2FyZS5jb20iLCJyb2xlIjoiQURNSU4iLCJpYXQiOjE3MzI0OTcyMDAsImV4cCI6MTczMjUwMDgwMH0.abc123...",
    "refreshToken": "..."
  }
}
```

**Lưu ý:** Copy `accessToken` để dùng cho các request tiếp theo.

---

## 🎯 BƯỚC 3: TEST ENDPOINT

### 3.1 Request Body Mẫu (Happy Path)

**Postman Request:**
```http
POST http://localhost:8222/api/v1/booking/submit-booking
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "bookingFor": "self",
  "patientRelativeName": "Nguyễn Văn A",
  "patientRelativePhone": "0987654321",
  "patientId": "patient-123",
  "patientName": "Nguyễn Văn A",
  "patientPhone": "0987654321",
  "patientEmail": "patient123@example.com",
  "patientBirthDate": "1995-05-15",
  "patientGender": "male",
  "patientAddress": "273 An Dương Vương, Phường 3, Quận 5, TP.HCM",
  "bookingReason": "Khám sức khỏe định kỳ",
  "clinicBranchId": "CLN001_BR001",
  "bookingPackageId": "PKGBK001",
  "packageScheduleId": "PKGSCHDL_PKG01_SLOT001_20251122",
  "purchaseMethod": "DIRECT"
}
```

### 3.2 Expected Response (Success)

**HTTP Status:** 200 OK

```json
{
  "status": 200,
  "message": "Booking created successfully",
  "data": {
    "orderId": "BKG_20251125_001"
  }
}
```

### 3.3 cURL Command (Alternative)

```powershell
curl -X POST http://localhost:8222/api/v1/booking/submit-booking `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" `
  -H "Content-Type: application/json" `
  -d '{
    "bookingFor": "self",
    "patientRelativeName": "Nguyen Van A",
    "patientRelativePhone": "0987654321",
    "patientId": "patient-123",
    "patientName": "Nguyen Van A",
    "patientPhone": "0987654321",
    "patientEmail": "patient123@example.com",
    "patientBirthDate": "1995-05-15",
    "patientGender": "male",
    "patientAddress": "273 An Duong Vuong, Phuong 3, Quan 5, TP.HCM",
    "bookingReason": "Kham suc khoe dinh ky",
    "clinicBranchId": "CLN001_BR001",
    "bookingPackageId": "PKGBK001",
    "packageScheduleId": "PKGSCHDL_PKG01_SLOT001_20251122",
    "purchaseMethod": "DIRECT"
  }'
```

---

## 🔍 BƯỚC 4: MONITOR SAGA FLOW

### 4.1 Mở Kafka UI (Recommended)

**URL:** http://localhost:8080

**Steps:**
1. Truy cập http://localhost:8080
2. Click vào topic `booking-events`
3. Gọi API submit-booking
4. Refresh page để xem event mới
5. Click vào message để xem chi tiết JSON

### 4.2 Monitor qua Terminal (Advanced)

**Terminal 1 - Booking Events:**
```powershell
docker exec -it booking-kafka kafka-console-consumer `
  --topic booking-events `
  --bootstrap-server localhost:9092 `
  --from-beginning `
  --property print.key=true `
  --property print.timestamp=true
```

**Expected Output:**
```json
CreateTime:1732497201000 BKG_20251125_001 {
  "eventType": "BookingCreatedEvent",
  "aggregateId": "BKG_20251125_001",
  "correlationId": "a1b2c3d4-uuid-123",
  "timestamp": "2025-11-25T10:00:01Z",
  "source": "booking-service",
  "payload": {
    "bookingId": "BKG_20251125_001",
    "packageScheduleId": "PKGSCHDL_PKG01_SLOT001_20251122",
    "patientId": "patient-123",
    "clinicId": "CLN001_BR001"
  }
}
```

**Terminal 2 - Schedule Events:**
```powershell
docker exec -it booking-kafka kafka-console-consumer `
  --topic schedule-events `
  --bootstrap-server localhost:9092 `
  --from-beginning
```

**Expected Output:**
```json
{
  "eventType": "HoldSlotSucceededEvent",
  "aggregateId": "BKG_20251125_001",
  "correlationId": "a1b2c3d4-uuid-123",
  "timestamp": "2025-11-25T10:00:02Z",
  "source": "schedule-service",
  "payload": {
    "bookingId": "BKG_20251125_001",
    "scheduleHoldId": "HOLD_123",
    "holdExpireAt": "2025-11-25T10:15:02Z",
    "packageScheduleId": "PKGSCHDL_PKG01_SLOT001_20251122"
  }
}
```

**Terminal 3 - Payment Events:**
```powershell
docker exec -it booking-kafka kafka-console-consumer `
  --topic payment-events `
  --bootstrap-server localhost:9092 `
  --from-beginning
```

**Terminal 4 - Service Logs:**
```powershell
# Booking Service logs
docker compose logs -f booking-service | Select-String "Booking|Saga|Event"

# Schedule Service logs
docker compose logs -f schedule-service | Select-String "Hold|Schedule"

# Payment Service logs
docker compose logs -f payment-service | Select-String "Payment"
```

**Expected Logs (Booking Service):**
```
[INFO] BookingController - Received booking command: CreateBookingCommand[bookingFor=self, ...]
[INFO] BookingApplicationService - Booking created: id=BKG_20251125_001, status=PENDING
[INFO] BookingApplicationService - Saga initiated: bookingId=BKG_20251125_001, correlationId=uuid-123
[INFO] BookingEventPublisher - Published BookingCreatedEvent: bookingId=BKG_20251125_001
[INFO] BookingSagaOrchestrator - Received event: type=HoldSlotSucceededEvent
[INFO] BookingSagaOrchestrator - Booking hold confirmed: bookingId=BKG_20251125_001
[INFO] BookingEventPublisher - Published PaymentRequestedEvent: bookingId=BKG_20251125_001
[INFO] BookingSagaOrchestrator - Received payment event: type=PaymentSucceededEvent
[INFO] BookingSagaOrchestrator - Payment confirmed: bookingId=BKG_20251125_001
[INFO] BookingSagaOrchestrator - Booking confirmed: bookingId=BKG_20251125_001, status=BOOKED
```

---

## ✅ BƯỚC 5: VERIFY KẾT QUẢ

### 5.1 Kiểm tra Database

**Connect to PostgreSQL:**
```powershell
docker exec -it bookingcare_postgresql psql -U postgres -d booking-service
```

**Query 1: Check Booking Status**
```sql
SELECT 
    id, 
    booking_status, 
    patient_id,
    package_schedule_id,
    created_at,
    updated_at
FROM health_check_package_schedule_booking_details 
WHERE id = 'BKG_20251125_001';
```

**Expected Result:**
```
┌──────────────────┬────────────────┬─────────────┬──────────────────────────────────┬─────────────────────┬─────────────────────┐
│ id               │ booking_status │ patient_id  │ package_schedule_id              │ created_at          │ updated_at          │
├──────────────────┼────────────────┼─────────────┼──────────────────────────────────┼─────────────────────┼─────────────────────┤
│ BKG_20251125_001 │ BOOKED         │ patient-123 │ PKGSCHDL_PKG01_SLOT001_20251122 │ 2025-11-25 10:00:01 │ 2025-11-25 10:00:05 │
└──────────────────┴────────────────┴─────────────┴──────────────────────────────────┴─────────────────────┴─────────────────────┘
```

**Query 2: Check Schedule Hold**
```sql
SELECT * FROM health_check_package_schedule_holds 
WHERE booking_id = 'BKG_20251125_001';
```

**Expected Result:**
```
┌──────────┬──────────────────┬────────┬─────────────────────┬─────────────────────┐
│ id       │ booking_id       │ status │ created_at          │ expire_at           │
├──────────┼──────────────────┼────────┼─────────────────────┼─────────────────────┤
│ HOLD_123 │ BKG_20251125_001 │ BOOKED │ 2025-11-25 10:00:02 │ 2025-11-25 10:15:02 │
└──────────┴──────────────────┴────────┴─────────────────────┴─────────────────────┘
```

**Query 3: Check Payment**
```sql
-- Connect to payment database
\c payment

SELECT 
    id, 
    booking_id, 
    status, 
    amount, 
    created_at 
FROM payments 
WHERE booking_id = 'BKG_20251125_001';
```

**Expected Result:**
```
┌─────────┬──────────────────┬────────┬─────────┬─────────────────────┐
│ id      │ booking_id       │ status │ amount  │ created_at          │
├─────────┼──────────────────┼────────┼─────────┼─────────────────────┤
│ PAY_123 │ BKG_20251125_001 │ PAID   │ 500000  │ 2025-11-25 10:00:03 │
└─────────┴──────────────────┴────────┴─────────┴─────────────────────┘
```

**Query 4: Verify Capacity Decreased**
```sql
-- Connect to schedule database
\c schedule

SELECT 
    id, 
    max_capacity, 
    current_capacity 
FROM health_check_package_schedules 
WHERE id = 'PKGSCHDL_PKG01_SLOT001_20251122';
```

**Expected Result:**
```
┌──────────────────────────────────┬──────────────┬──────────────────┐
│ id                               │ max_capacity │ current_capacity │
├──────────────────────────────────┼──────────────┼──────────────────┤
│ PKGSCHDL_PKG01_SLOT001_20251122 │ 10           │ 9                │ ← Giảm 1
└──────────────────────────────────┴──────────────┴──────────────────┘
```

### 5.2 Verify via API

**Get Booking by ID:**
```http
GET http://localhost:8222/api/v1/booking/BKG_20251125_001
Authorization: Bearer {{accessToken}}
```

**Expected Response:**
```json
{
  "status": 200,
  "message": "Bookings fetched successfully",
  "data": {
    "id": "BKG_20251125_001",
    "bookingStatus": "BOOKED",
    "patientInfo": {
      "patientId": "patient-123",
      "patientName": "Nguyễn Văn A",
      "patientPhone": "0987654321"
    },
    "clinicInfo": {
      "clinicFullName": "Phòng khám ABC",
      "clinicBranchName": "Chi nhánh Quận 5",
      "clinicBranchAddress": "273 An Dương Vương"
    },
    "createdAt": "2025-11-25T10:00:01Z",
    "updatedAt": "2025-11-25T10:00:05Z"
  }
}
```

---

## 🧪 BƯỚC 6: TEST SCENARIOS

### Scenario 1: Happy Path ✅

**Input:**
```json
{
  "packageScheduleId": "PKGSCHDL_PKG01_SLOT001_20251122",
  "patientId": "patient-123"
}
```

**Expected Flow:**
1. ✅ Booking created (PENDING)
2. ✅ BookingCreatedEvent published
3. ✅ Schedule holds slot
4. ✅ HoldSlotSucceededEvent published
5. ✅ Booking → PENDING_PAYMENT
6. ✅ PaymentRequestedEvent published
7. ✅ Payment created
8. ✅ PaymentSucceededEvent published
9. ✅ Booking → BOOKED
10. ✅ Schedule confirmed

**Database State:**
- Booking: `BOOKED`
- Schedule Hold: `BOOKED`
- Payment: `PAID`
- Capacity: Decreased by 1

---

### Scenario 2: Invalid Schedule (Compensation) ❌

**Input:**
```json
{
  "packageScheduleId": "INVALID_SCHEDULE_ID",
  "patientId": "patient-123"
}
```

**Expected Flow:**
1. ✅ Booking created (PENDING)
2. ✅ BookingCreatedEvent published
3. ❌ Schedule service cannot find schedule
4. ✅ HoldSlotFailedEvent published
5. ✅ Booking → CANCELLED

**Verify:**
```sql
SELECT booking_status FROM health_check_package_schedule_booking_details 
WHERE id = 'BKG_XXX';
-- Expected: CANCELLED
```

---

### Scenario 3: Schedule Full (No Capacity) ❌

**Input:**
```json
{
  "packageScheduleId": "PKGSCHDL_FULL",  // current_capacity = 0
  "patientId": "patient-123"
}
```

**Expected:**
- HoldSlotFailedEvent published
- Booking → CANCELLED
- No payment created

---

### Scenario 4: Payment Timeout ⏱️

**Input:** Valid request nhưng không thanh toán

**Expected:**
- Hold expires after 15 minutes
- PaymentFailedEvent (if webhook timeout)
- Booking → CANCELLED
- Schedule capacity restored

---

## 📊 BƯỚC 7: POSTMAN COLLECTION

### Import Collection

**File Location:** `backend/services/booking/BookingService-API.postman_collection.json`

**Steps:**
1. Mở Postman
2. Click **Import** → **File**
3. Chọn `BookingService-API.postman_collection.json`
4. Set environment variable `{{access_token}}`
5. Run "Submit Booking" request

### Environment Variables

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "base_url": "http://localhost:8222",
  "booking_id": "BKG_20251125_001"
}
```

---

## 🐛 TROUBLESHOOTING

### Issue 1: 403 Forbidden

**Error:**
```json
{
  "status": 403,
  "message": "Access Denied"
}
```

**Cause:** Token expired hoặc không có quyền

**Solution:**
```bash
# Login lại để lấy token mới
POST http://localhost:8222/api/v1/auth/sign-in
```

---

### Issue 2: Events không được consume

**Symptoms:** Booking status không thay đổi sau khi tạo

**Check Consumer Groups:**
```powershell
docker exec booking-kafka kafka-consumer-groups --list --bootstrap-server localhost:9092
```

**Expected Output:**
```
booking-saga-group
schedule-group
payment-group
```

**Check Consumer Lag:**
```powershell
docker exec booking-kafka kafka-consumer-groups `
  --describe `
  --group booking-saga-group `
  --bootstrap-server localhost:9092
```

**Expected:** LAG = 0 (all events processed)

**If LAG > 0:**
```powershell
# Restart consumers
docker compose restart booking-service schedule-service payment-service
```

---

### Issue 3: Kafka Connection Error

**Error in Logs:**
```
ERROR: Connection to node -1 (kafka/172.18.0.5:29092) could not be established
```

**Cause:** Service config vẫn dùng `localhost:9092` thay vì `kafka:29092`

**Solution:**
1. Check `booking-service.yml`:
```yaml
spring:
  kafka:
    bootstrap-servers: kafka:29092  # PHẢI là kafka:29092, KHÔNG phải localhost:9092
```

2. Restart config-server và services:
```powershell
docker compose restart config-server
docker compose restart booking-service schedule-service payment-service
```

---

### Issue 4: Database Connection Error

**Error:**
```
Connection refused: postgresql:5432
```

**Check PostgreSQL:**
```powershell
docker exec bookingcare_postgresql pg_isready -U postgres
```

**Expected:** `postgresql:5432 - accepting connections`

**If not ready:**
```powershell
docker compose restart postgresql
```

---

### Issue 5: Service không khởi động

**Check logs:**
```powershell
docker compose logs booking-service | Select-String "ERROR|Exception"
```

**Common Issues:**
- Port conflict: Change port in `docker-compose.yml`
- Database migration failed: Check Flyway scripts
- Config server not ready: Wait 30 seconds after `docker compose up`

---

## 📈 PERFORMANCE TESTING

### Load Test với Artillery

**Install Artillery:**
```powershell
npm install -g artillery
```

**Create test config (`artillery-test.yml`):**
```yaml
config:
  target: "http://localhost:8222"
  phases:
    - duration: 60
      arrivalRate: 10
      name: "Sustained load"
  http:
    headers:
      Authorization: "Bearer YOUR_TOKEN"
      Content-Type: "application/json"

scenarios:
  - name: "Create Booking"
    flow:
      - post:
          url: "/api/v1/booking/submit-booking"
          json:
            bookingFor: "self"
            patientId: "patient-{{ $randomNumber(1, 1000) }}"
            packageScheduleId: "PKGSCHDL_PKG01_SLOT001_20251122"
            clinicBranchId: "CLN001_BR001"
```

**Run test:**
```powershell
artillery run artillery-test.yml
```

**Expected Metrics:**
- Response time: < 500ms (p95)
- Success rate: > 99%
- Kafka lag: 0
- No errors

---

## ✅ SUCCESS CRITERIA

### ✓ API Response
- HTTP Status: 200
- Message: "Booking created successfully"
- Data contains: `orderId`

### ✓ Kafka Events
- BookingCreatedEvent published ✅
- HoldSlotSucceededEvent received ✅
- PaymentRequestedEvent published ✅
- PaymentSucceededEvent received ✅
- BookingConfirmedEvent published ✅

### ✓ Database State
- Booking status: `BOOKED` ✅
- Schedule hold: `BOOKED` ✅
- Payment status: `PAID` ✅
- Capacity decreased: ✅

### ✓ Logs
- No errors ✅
- All saga steps completed ✅
- Correlation ID tracked across services ✅

---

## 📚 REFERENCE DOCUMENTS

- **Saga Flow Documentation:** `backend/docs/SAGA_HAPPY_FLOW_DOCUMENTATION.md`
- **Verification Report:** `backend/services/booking/SAGA_VERIFICATION_REPORT.md`
- **Test Script:** `backend/test-saga-flow.ps1`
- **Postman Collection:** `backend/services/booking/BookingService-API.postman_collection.json`

---

## 🎉 SUMMARY

**Endpoint đã test:**
```java
@PostMapping("submit-booking")
public ApiResponse<CreateBookingResponse> createBookingOrder(@RequestBody CreateBookingCommand command) {
    String orderId = _bookingApplicationService.createBooking(command);
    CreateBookingResponse response = new CreateBookingResponse(orderId);
    return new ApiResponse<>(200, "Booking created successfully", response);
}
```

**Saga Pattern Flow:**
1. ✅ Client → Booking Service
2. ✅ Booking Service → Kafka (BookingCreatedEvent)
3. ✅ Schedule Service → Hold slot
4. ✅ Schedule Service → Kafka (HoldSlotSucceededEvent)
5. ✅ Booking Saga → Update booking status
6. ✅ Booking Saga → Kafka (PaymentRequestedEvent)
7. ✅ Payment Service → Create payment
8. ✅ Payment Service → Kafka (PaymentSucceededEvent)
9. ✅ Booking Saga → Confirm booking
10. ✅ Booking Saga → Kafka (BookingConfirmedEvent)
11. ✅ Schedule Service → Confirm hold

**All systems operational! 🚀**
