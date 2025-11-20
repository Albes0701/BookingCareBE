CREATE TYPE payment_status AS ENUM (
    'PENDING',
    'COMPLETED',
    'FAILED',
    'CANCELLED'
);

CREATE TABLE payments (
    "id" VARCHAR(255) PRIMARY KEY,
    "booking_id" VARCHAR(255) UNIQUE,
    "order_code" BIGINT NOT NULL UNIQUE,
    "amount" BIGINT NOT NULL,
    "description" VARCHAR(255),
    "status" VARCHAR(50),
    "payment_date" TIMESTAMP
);