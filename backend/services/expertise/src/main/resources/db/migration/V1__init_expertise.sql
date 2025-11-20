-- === specialties ============================================================
CREATE TABLE IF NOT EXISTS specialties (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    specialty_detail_infor TEXT,
    slug VARCHAR(255) NOT NULL UNIQUE,
    image VARCHAR(2048),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);

-- index theo @Index (dù slug đã UNIQUE vẫn tạo riêng để khớp entity)
CREATE INDEX IF NOT EXISTS idx_specialties_slug ON specialties (slug);

-- === doctors ================================================================
CREATE TABLE IF NOT EXISTS doctors (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    short_doctor_infor VARCHAR(255),
    doctor_detail_infor TEXT,
    slug VARCHAR(255) NOT NULL UNIQUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_doctors_slug ON doctors (slug);
CREATE INDEX IF NOT EXISTS idx_doctors_user_id ON doctors (user_id);

-- === doctors_specialties (bảng nối) ========================================
CREATE TABLE IF NOT EXISTS doctors_specialties (
    id UUID PRIMARY KEY,
    doctor_id UUID NOT NULL,
    specialty_id UUID NOT NULL,
    CONSTRAINT uk_doctor_specialty UNIQUE (doctor_id, specialty_id)
);

-- Indexes trên cột FK như annotation
CREATE INDEX IF NOT EXISTS idx_ds_doctor ON doctors_specialties (doctor_id);
CREATE INDEX IF NOT EXISTS idx_ds_specialty ON doctors_specialties (specialty_id);

-- Ràng buộc khóa ngoại (mặc định NO ACTION theo entity)
ALTER TABLE doctors_specialties
    ADD CONSTRAINT fk_ds_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors (id);

ALTER TABLE doctors_specialties
    ADD CONSTRAINT fk_ds_specialty
        FOREIGN KEY (specialty_id) REFERENCES specialties (id);
