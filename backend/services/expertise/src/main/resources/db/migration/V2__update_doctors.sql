ALTER TABLE doctors
    ALTER COLUMN short_doctor_infor TYPE varchar(255) USING short_doctor_infor::varchar,
    ALTER COLUMN doctor_detail_infor TYPE text USING doctor_detail_infor::text;
