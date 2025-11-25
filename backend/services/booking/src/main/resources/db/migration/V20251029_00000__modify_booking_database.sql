-- Add clinic_branch_id column to existing table
ALTER TABLE health_check_package_schedule_booking_details
ADD COLUMN clinic_branch_id VARCHAR(50);

-- Create patients table (if not exists)
CREATE TABLE IF NOT EXISTS patients
(
    patient_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (patient_id)
);

-- Add foreign key constraint
ALTER TABLE health_check_package_schedule_booking_details
ADD CONSTRAINT FK_booking_patient_id
FOREIGN KEY (patient_id)
REFERENCES patients(patient_id);