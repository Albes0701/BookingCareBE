ALTER TABLE doctors
    ALTER COLUMN doctor_detail_infor TYPE text
    USING doctor_detail_infor::text;
