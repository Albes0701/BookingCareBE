-- Create the schedules table
CREATE TABLE schedules (
    id VARCHAR(255) NOT NULL,
    start_time VARCHAR(255) NOT NULL,
    end_time VARCHAR(255) NOT NULL,
    day_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

-- ============================================
-- 2. Bảng lịch gói khám theo ngày (slot cụ thể)
-- Mỗi record = 1 slot của 1 package vào 1 ngày cụ thể
-- ============================================

CREATE TABLE health_check_package_schedules (
    package_schedule_id VARCHAR(255) NOT NULL,  -- PK slot cụ thể
    package_id          VARCHAR(255) NOT NULL,
    schedule_id         VARCHAR(255) NOT NULL,
    schedule_date       DATE         NOT NULL,

    -- [ADDED] số lượng bệnh nhân tối đa/slot
    capacity            INT          NOT NULL DEFAULT 1,

    -- [ADDED] số booking đã CONFIRMED cho slot này
    booked_count        INT          NOT NULL DEFAULT 0,

    -- [ADDED] cho phép overbook thêm bao nhiêu người (0 = không cho overbook)
    overbook_limit      INT          NOT NULL DEFAULT 0,

    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- [ADDED] updated_at để trace cập nhật schedule
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (package_schedule_id)
);

-- Add the foreign key constraint
ALTER TABLE health_check_package_schedules
ADD CONSTRAINT FK_schedule_id
FOREIGN KEY (schedule_id)
REFERENCES schedules(id);


-- [EXISTING] Composite index để query nhanh các slot theo package + ngày
CREATE INDEX idx_package_schedule_date
    ON health_check_package_schedules (package_id, schedule_date);

-- [ADDED] Index hỗ trợ query theo ngày (cho bác sĩ / admin xem slot trong ngày)
CREATE INDEX idx_schedule_date
    ON health_check_package_schedules (schedule_date);

-- ============================================
-- 3. Bảng HOLD/BOOKED theo slot
-- Mỗi record = 1 hold/booking gắn vào 1 package_schedule_id
-- ============================================

CREATE TABLE schedule_holds (
    id                  VARCHAR(255) PRIMARY KEY,        -- Hxxx
    package_schedule_id VARCHAR(255) NOT NULL,
    booking_id          VARCHAR(255) NOT NULL,           -- id booking bên BookingService

    -- [ADDED] trạng thái hold/booking trên slot:
    --  HOLD    : đã giữ chỗ, chờ thanh toán
    --  BOOKED  : đã confirm (booking_status = CONFIRMED)
    --  EXPIRED : hold hết hạn mà không confirm
    --  RELEASED: release do payment fail / user huỷ
    status              VARCHAR(20)  NOT NULL,

    expire_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- [ADDED] FK nội bộ tới slot
ALTER TABLE schedule_holds
    ADD CONSTRAINT fk_schedule_holds_package_schedule
        FOREIGN KEY (package_schedule_id)
            REFERENCES health_check_package_schedules(package_schedule_id);

-- [ADDED] Index cho các hold/booking đang active trên slot
--  Thường dùng để:
--   - tính active HOLD/BOOKED,
--   - check capacity khi giữ/confirm
CREATE INDEX idx_schedule_holds_active
    ON schedule_holds (package_schedule_id)
    WHERE status IN ('HOLD', 'BOOKED');


-- Tránh tạo 2 slot y chang cho cùng 1 package cùng ngày + cùng khung giờ
CREATE UNIQUE INDEX ux_hcps_pkg_slot
ON health_check_package_schedules (package_id, schedule_id, schedule_date)
WHERE is_deleted = FALSE;


-- ============================================
-- 4. Gợi ý logic (không phải SQL, chỉ comment)
-- ============================================

-- Khi Booking gọi "holdSlot":
-- 1) Trong 1 transaction ở Schedule:
--    - SELECT capacity, booked_count,
--      active_hold_count = COUNT(HOLD/BOOKED còn chưa expire)
--    - available = capacity + overbook_limit - max(booked_count, active_hold_count)
--    - nếu available <= 0 -> báo hết chỗ
--    - nếu còn chỗ -> INSERT vào schedule_holds (status=HOLD, expire_at=now()+X phút)
--
-- Khi Booking confirm sau thanh toán:
-- 1) Trong 1 transaction:
--    - SELECT hold + health_check_package_schedules FOR UPDATE
--    - Kiểm tra hold.status = HOLD & expire_at > now()
--    - Kiểm tra booked_count < capacity + overbook_limit
--    - UPDATE schedule_holds SET status='BOOKED'
--    - UPDATE health_check_package_schedules SET booked_count = booked_count + 1
--
-- Khi payment fail / user huỷ:
--    - UPDATE schedule_holds SET status='RELEASED' WHERE status='HOLD'
--    - booked_count KHÔNG đổi (vì chưa BOOKED)
--
-- Cron job expire:
--    - UPDATE schedule_holds SET status='EXPIRED'
--      WHERE status='HOLD' AND expire_at < now();


-- ============================================================================= --
-- Trigger kiểm tra tính nhất quán giữa schedule_date và day_id của schedule     --
-- ============================================================================= --

-- Hàm kiểm tra
CREATE OR REPLACE FUNCTION check_schedule_day_consistency()
RETURNS trigger AS $$
DECLARE
    v_day_id   VARCHAR(10);
    v_date_dow VARCHAR(10);
BEGIN
    -- Lấy day_id từ bảng schedules
    SELECT day_id
      INTO v_day_id
    FROM schedules
    WHERE id = NEW.schedule_id;

    IF v_day_id IS NULL THEN
        RAISE EXCEPTION 'schedule_id % không tồn tại trong schedules', NEW.schedule_id;
    END IF;

    -- Lấy thứ thực tế của schedule_date: MON/TUE/WED/...
    -- Bạn có thể dùng 'DY' (MON,TUE,...) và upper() cho khớp với day_id
    v_date_dow := UPPER(to_char(NEW.schedule_date, 'DY'));

    -- Nếu không trùng thì reject
    IF v_day_id <> v_date_dow THEN
        RAISE EXCEPTION
            'schedule_date % là %, không trùng day_id % của schedule_id %',
            NEW.schedule_date, v_date_dow, v_day_id, NEW.schedule_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Gắn trigger vào bảng health_check_package_schedules
CREATE TRIGGER trg_check_schedule_day
BEFORE INSERT OR UPDATE ON health_check_package_schedules
FOR EACH ROW
EXECUTE FUNCTION check_schedule_day_consistency();





















