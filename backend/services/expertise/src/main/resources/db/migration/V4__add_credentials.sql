CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1️⃣ Bảng loại chứng chỉ
CREATE TABLE credential_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2️⃣ Bảng chứng chỉ của bác sĩ
CREATE TABLE doctor_credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL,
    credential_type_id UUID NOT NULL,
    license_number VARCHAR(128) NOT NULL,
    issuer VARCHAR(255) NOT NULL,
    country_code VARCHAR(2),
    region VARCHAR(128),
    issue_date DATE,
    expiry_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',   -- PENDING / APPROVED / REJECTED / EXPIRED
    note TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),

    -- 🔗 Quan hệ
    CONSTRAINT fk_doctor FOREIGN KEY (doctor_id)
        REFERENCES doctors (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT fk_credential_type FOREIGN KEY (credential_type_id)
        REFERENCES credential_types (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT uk_doctor_license UNIQUE (doctor_id, credential_type_id, license_number)
);

CREATE INDEX idx_dc_doctor_id ON doctor_credentials (doctor_id);
CREATE INDEX idx_dc_type_id ON doctor_credentials (credential_type_id);
CREATE INDEX idx_dc_status ON doctor_credentials (status);
CREATE INDEX idx_dc_expiry ON doctor_credentials (expiry_date);

-- 3️⃣ File đính kèm của chứng chỉ
CREATE TABLE doctor_credential_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_credential_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    file_url TEXT NOT NULL,
    uploaded_at TIMESTAMPTZ DEFAULT NOW(),

    -- 🔗 Quan hệ
    CONSTRAINT fk_dcf_credential FOREIGN KEY (doctor_credential_id)
        REFERENCES doctor_credentials (id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX idx_dcf_credential ON doctor_credential_files (doctor_credential_id);

-- 4️⃣ Log xác thực (audit trail)
CREATE TABLE doctor_credential_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_credential_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL,          -- SUBMIT / REVIEW / APPROVE / REJECT / AUTO_EXPIRE
    actor_id UUID,
    comment TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),

    -- 🔗 Quan hệ
    CONSTRAINT fk_dcv_credential FOREIGN KEY (doctor_credential_id)
        REFERENCES doctor_credentials (id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX idx_dcv_credential ON doctor_credential_verifications (doctor_credential_id);
