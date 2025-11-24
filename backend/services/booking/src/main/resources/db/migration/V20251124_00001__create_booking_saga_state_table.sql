-- Create booking_saga_state table to track saga orchestration state
CREATE TABLE booking_saga_state
(
    id VARCHAR(255) NOT NULL,
    booking_id VARCHAR(255) NOT NULL UNIQUE,
    correlation_id VARCHAR(255) NOT NULL,
    
    saga_status VARCHAR(50) NOT NULL,
    current_saga_step VARCHAR(50) NOT NULL,
    
    schedule_hold_id VARCHAR(255),
    hold_expire_at TIMESTAMP WITH TIME ZONE,
    
    external_payment_id VARCHAR(255),
    last_payment_status VARCHAR(50),
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (booking_id) REFERENCES health_check_package_schedule_booking_details(id)
);

CREATE INDEX idx_saga_state_booking_id ON booking_saga_state(booking_id);
CREATE INDEX idx_saga_state_correlation_id ON booking_saga_state(correlation_id);
CREATE INDEX idx_saga_state_status ON booking_saga_state(saga_status);
