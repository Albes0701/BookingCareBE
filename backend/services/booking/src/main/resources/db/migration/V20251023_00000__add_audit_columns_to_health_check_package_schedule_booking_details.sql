ALTER TABLE health_check_package_schedule_booking_details
ADD COLUMN created_date TIMESTAMP
WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_date TIMESTAMP
WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Update existing records to have current timestamp
UPDATE health_check_package_schedule_booking_details 
SET created_date = CURRENT_TIMESTAMP, updated_date = CURRENT_TIMESTAMP 
WHERE created_date IS NULL OR updated_date IS NULL;