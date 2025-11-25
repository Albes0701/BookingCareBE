ALTER TABLE health_check_package_schedule_booking_details  -- ✅ THÊM 'S'
ADD COLUMN payment_url VARCHAR(500),
ADD COLUMN order_code BIGINT;

CREATE INDEX idx_booking_order_code ON health_check_package_schedule_booking_details(order_code);  -- ✅ THÊM 'S'