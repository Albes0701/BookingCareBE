-- Create the booking_packages table
CREATE TABLE booking_packages
(
    id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

-- Create the booking_packages_details table
CREATE TABLE booking_packages_details
(
    package_id VARCHAR(255) NOT NULL,
    booking_package_id VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    PRIMARY KEY (package_id, booking_package_id)
);

-- Create the health_check_package_schedule_booking_details table
CREATE TABLE health_check_package_schedule_booking_details
(
    id VARCHAR(255) NOT NULL,
    patient_relatives_name VARCHAR(255),
    patient_relatives_phone_number VARCHAR(50),
    patient_id VARCHAR(255) NOT NULL,
    package_schedule_id VARCHAR(255) NOT NULL,
    booking_package_id VARCHAR(255) NOT NULL,
    booking_reason TEXT,
    doctor_id VARCHAR(255),
    clinic_id VARCHAR(255) NOT NULL,
    booking_status VARCHAR(50) NOT NULL,
    purchase_method VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);

-- Add the foreign key constraint
ALTER TABLE booking_packages_details
ADD CONSTRAINT FK_booking_package_id
FOREIGN KEY (booking_package_id)
REFERENCES booking_packages(id);

ALTER TABLE health_check_package_schedule_booking_details
ADD CONSTRAINT FK_booking_package_detail_id
FOREIGN KEY (booking_package_id)
REFERENCES booking_packages(id);

-- Composite index for faster queries by package_id + schedule_date
CREATE INDEX idx_patient_id ON health_check_package_schedule_booking_details (patient_id);
CREATE INDEX idx_clinic_id ON health_check_package_schedule_booking_details (clinic_id);

ALTER TABLE health_check_package_schedule_booking_details
ADD COLUMN created_date TIMESTAMP NULL,
ADD COLUMN updated_date TIMESTAMP NULL;
