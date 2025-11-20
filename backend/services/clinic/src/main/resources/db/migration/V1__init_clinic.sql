-- Enable UUID helper for default values if desired by other migrations
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ==========================================
-- Clinics core table (matches Clinic entity)
-- ==========================================
CREATE TABLE IF NOT EXISTS clinics (
  id                 VARCHAR(36) PRIMARY KEY,
  fullname           VARCHAR(255),
  name               VARCHAR(255) NOT NULL,
  address            TEXT,
  clinic_detail_info TEXT,
  image              VARCHAR(255),
  slug               VARCHAR(255) UNIQUE,
  status             VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
  created_by_user_id VARCHAR(36) NOT NULL,
  is_deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON COLUMN clinics.id IS 'Generated UUID string';
COMMENT ON COLUMN clinics.status IS 'Clinic workflow status';
COMMENT ON COLUMN clinics.created_by_user_id IS 'Owner user id';

CREATE INDEX IF NOT EXISTS idx_clinics_created_by_user_id
  ON clinics (created_by_user_id);

-- ==============================================
-- Clinic branches (matches ClinicBranch entity)
-- ==============================================

CREATE TABLE IF NOT EXISTS clinic_branches (
  id                     VARCHAR(36) PRIMARY KEY,
  clinic_id              VARCHAR(36) NOT NULL,
  clinic_branch_name     VARCHAR(255) NOT NULL,
  clinic_branch_address  VARCHAR(255),
  is_deleted             BOOLEAN NOT NULL DEFAULT FALSE, -- ADDED: branch archive/restore via is_deleted (no workflow for branch)
  CONSTRAINT fk_branch_clinic
    FOREIGN KEY (clinic_id) REFERENCES clinics (id)
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_clinic_branches_clinic_id
  ON clinic_branches (clinic_id);

-- =====================================================
-- Branch-doctor assignments (ClinicBranchDoctor entity)
-- =====================================================
CREATE TABLE IF NOT EXISTS clinic_branch_doctors (
  id               VARCHAR(36) PRIMARY KEY,
  clinic_branch_id VARCHAR(36) NOT NULL,
  doctor_id        VARCHAR(255) NOT NULL,
  is_deleted       BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_branch_doctor
    FOREIGN KEY (clinic_branch_id) REFERENCES clinic_branches (id)
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT uq_branch_doctor UNIQUE (clinic_branch_id, doctor_id)
);

CREATE INDEX IF NOT EXISTS idx_branch_doctors_branch_id
  ON clinic_branch_doctors (clinic_branch_id);

CREATE INDEX IF NOT EXISTS idx_branch_doctors_doctor_id
  ON clinic_branch_doctors (doctor_id);

-- =====================================================================
-- Branch-healthcheck packages (ClinicBranchHealthcheckPackage entity)
-- =====================================================================
CREATE TABLE IF NOT EXISTS clinic_branch_healthcheck_packages (
  id                     VARCHAR(36) PRIMARY KEY,
  clinic_branch_id       VARCHAR(36) NOT NULL,
  healthcheck_package_id VARCHAR(255) NOT NULL,
  is_deleted             BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_branch_package
    FOREIGN KEY (clinic_branch_id) REFERENCES clinic_branches (id)
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT uq_branch_package UNIQUE (clinic_branch_id, healthcheck_package_id)
);

CREATE INDEX IF NOT EXISTS idx_branch_packages_branch_id
  ON clinic_branch_healthcheck_packages (clinic_branch_id);

CREATE INDEX IF NOT EXISTS idx_branch_packages_package_id
  ON clinic_branch_healthcheck_packages (healthcheck_package_id);

-- ===========================================================
-- Verification log (matches ClinicVerification entity)
-- ===========================================================
CREATE TABLE IF NOT EXISTS clinic_verifications (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  clinic_id  VARCHAR(36) NOT NULL,
  action     VARCHAR(20) NOT NULL,
  actor_id   UUID NOT NULL,
  comment    TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_clinic_verification
    FOREIGN KEY (clinic_id) REFERENCES clinics (id)
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cv_clinic_created_at
  ON clinic_verifications (clinic_id, created_at DESC);
