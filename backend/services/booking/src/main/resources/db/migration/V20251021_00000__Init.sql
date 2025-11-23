-- ============================================
-- Booking Service Domain (BookingDomain.sql)
-- ============================================

-- ============================================
-- 1. Bảng danh mục gói khám
-- ============================================

-- [EXISTING]
-- Danh sách gói khám (ví dụ: Gói khám tổng quát, Gói khám tim mạch...)
CREATE TABLE booking_packages
(
    id          VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

-- [EXISTING]
-- Chi tiết gói khám theo package (giá, mô tả...)
CREATE TABLE booking_packages_details
(
    package_id         VARCHAR(255) NOT NULL,
    booking_package_id VARCHAR(255) NOT NULL,
    price              DECIMAL(10, 2) NOT NULL,
    description        TEXT,
    PRIMARY KEY (package_id, booking_package_id)
);

-- [EXISTING]
-- FK: booking_package_id -> booking_packages(id)
ALTER TABLE booking_packages_details
ADD CONSTRAINT FK_booking_package_id
FOREIGN KEY (booking_package_id)
REFERENCES booking_packages(id);



-- ============================================
-- 2. Bảng Booking chính cho ca khám
-- ============================================
-- Mỗi record = 1 ca khám (1 booking) gắn với 1 slot cụ thể (package_schedule_id)
-- Bảng này là trung tâm để:
--  - PATIENT xem lịch sử khám
--  - DOCTOR xem lịch trong ngày/tuần
--  - ADMIN/CLINIC thống kê số ca, no-show, doanh thu

CREATE TABLE health_check_package_schedule_booking_details
(
    id                             VARCHAR(255) NOT NULL,  -- PK booking

    -- Thông tin người thân (nếu booking hộ)
    patient_relatives_name         VARCHAR(255),
    patient_relatives_phone_number VARCHAR(50),

    -- Thông tin liên kết domain
    patient_id                     VARCHAR(255) NOT NULL,  -- FK -> patients(patient_id)
    package_schedule_id            VARCHAR(255) NOT NULL,  -- liên kết slot bên ScheduleService
    booking_package_id             VARCHAR(255) NOT NULL,  -- FK -> booking_packages(id)
    clinic_id                      VARCHAR(255) NOT NULL,
    clinic_branch_id               VARCHAR(50),            -- [ADDED] chi nhánh phòng khám (nếu có)

    -- [ADDED] Bác sĩ phụ trách ca khám này
    doctor_id                      VARCHAR(255),           -- dùng để DOCTOR filter lịch của mình

    -- Lý do đặt lịch, ghi chú thêm từ bệnh nhân
    booking_reason                 TEXT,

    -- Trạng thái booking theo lifecycle đặt lịch / thanh toán
    booking_status                 VARCHAR(50) NOT NULL,
    -- Gợi ý giá trị: PENDING_SCHEDULE, PENDING_PAYMENT, CONFIRMED,
    --                CANCELLED, EXPIRED, REJECTED_NO_SLOT, FAILED_NO_SLOT_AFTER_PAYMENT...

    -- [ADDED] Trạng thái thực tế của buổi khám tại phòng khám
    visit_status                   VARCHAR(50),
    -- Gợi ý giá trị: WAITING, CHECKED_IN, COMPLETED, NO_SHOW, CANCELLED_BY_DOCTOR

    -- Phương thức mua / thanh toán (ONLINE, OFFLINE, INSURANCE, ...)
    purchase_method                VARCHAR(50) NOT NULL,

    -- ==== Thông tin liên quan tới HOLD SLOT (Schedule Service) ====
    -- [ADDED] id hold trả về từ ScheduleService (schedule_holds.id)
    schedule_hold_id               VARCHAR(255),
    -- [ADDED] thời điểm expire của hold (để Booking có thể tự check/hiển thị)
    hold_expire_at                 TIMESTAMP WITH TIME ZONE,

    -- ==== Thông tin cache Payment ====
    -- [ADDED] id payment bên PaymentService (để trace/debug)
    external_payment_id            VARCHAR(255),
    -- [ADDED] trạng thái payment gần nhất (PENDING/COMPLETED/FAILED/CANCELLED)
    last_payment_status            VARCHAR(50),

    -- Timestamps
    created_date                   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date                   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id)
);

-- FK: booking_package_id -> booking_packages(id)
ALTER TABLE health_check_package_schedule_booking_details
ADD CONSTRAINT FK_booking_package_detail_id
FOREIGN KEY (booking_package_id)
REFERENCES booking_packages(id);




-- ============================================
-- 3. Indexes hỗ trợ query thường xuyên
-- ============================================

-- [EXISTING] Query booking theo patient
CREATE INDEX idx_booking_patient_id
    ON health_check_package_schedule_booking_details (patient_id);

-- [EXISTING] Query booking theo clinic
CREATE INDEX idx_booking_clinic_id
    ON health_check_package_schedule_booking_details (clinic_id);

-- [ADDED] Query booking theo doctor + booking_status
--  -> để DOCTOR xem lịch của mình theo trạng thái (CONFIRMED, ...)
CREATE INDEX idx_booking_doctor_status
    ON health_check_package_schedule_booking_details (doctor_id, booking_status);

-- [ADDED] Query booking theo slot (package_schedule_id)
--  -> để thống kê / join với Schedule theo slot
CREATE INDEX idx_booking_package_schedule_id
    ON health_check_package_schedule_booking_details (package_schedule_id);

-- [ADDED] Query theo booking_status (patient/doctor filter theo trạng thái)
CREATE INDEX idx_booking_status
    ON health_check_package_schedule_booking_details (booking_status);

-- [ADDED] Query theo visit_status (báo cáo no-show/completed)
CREATE INDEX idx_booking_visit_status
    ON health_check_package_schedule_booking_details (visit_status);



-- ============================================
-- 5. Transactional Outbox cho Booking Service
-- ============================================
-- Dùng để publish event BookingCreated, BookingConfirmed, BookingCancelled...
-- một cách an toàn (trong cùng transaction với DB Booking).

-- [ADDED]
CREATE TABLE booking_outbox_event
(
    id             BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(50)  NOT NULL,    -- ví dụ: 'BOOKING'
    aggregate_id   VARCHAR(255) NOT NULL,    -- id booking
    type           VARCHAR(100) NOT NULL,    -- BookingCreated, BookingConfirmed, BookingCancelled...

    payload        TEXT         NOT NULL,    -- JSON event

    processed      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at   TIMESTAMP WITH TIME ZONE
);

-- [ADDED] Index cho việc pick các event chưa được gửi lên Kafka
CREATE INDEX idx_booking_outbox_unprocessed
    ON booking_outbox_event (processed, created_at);



-- ============================================
-- 6. Bảng lưu event đã xử lý (idempotent consumer)
-- ============================================
-- Booking Service sẽ consume các event PaymentSucceeded/PaymentFailed từ Kafka.
-- Để tránh xử lý trùng event, sử dụng bảng processed_event để lưu event_id đã xử lý.

-- [ADDED]
CREATE TABLE booking_processed_event
(
    id           BIGSERIAL PRIMARY KEY,
    event_id     VARCHAR(255) NOT NULL UNIQUE,  -- eventId trong payload Kafka
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
