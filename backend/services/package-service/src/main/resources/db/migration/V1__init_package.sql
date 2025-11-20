-- Enable UUID helper used by default values.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- Approval status enum shared across health check packages.
-- ============================================================
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'approval_status') THEN
    CREATE TYPE approval_status AS ENUM ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED');
  END IF;
END
$$;

-- ==============================================
-- Package categories (PackageType entity).
-- ==============================================
CREATE TABLE IF NOT EXISTS package_type (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name       VARCHAR(255) NOT NULL,
  image      VARCHAR(255),
  slug       VARCHAR(255) NOT NULL,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE package_type IS 'Health check package categories';
CREATE UNIQUE INDEX IF NOT EXISTS ux_package_type_slug
  ON package_type (slug);

-- ==============================================
-- Medical services (parent taxonomy).
-- ==============================================
CREATE TABLE IF NOT EXISTS medical_service (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name       VARCHAR(255) NOT NULL,
  image      VARCHAR(255),
  slug       VARCHAR(255) NOT NULL,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_medical_service_slug
  ON medical_service (slug);

-- ==================================================
-- Specific medical services (child taxonomy).
-- ==================================================
CREATE TABLE IF NOT EXISTS specific_medical_service (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name        VARCHAR(255) NOT NULL,
  image       VARCHAR(255),
  slug        VARCHAR(255) NOT NULL,
  description TEXT,
  is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_specific_medical_service_slug
  ON specific_medical_service (slug);

-- =======================================================================
-- Specific-medical ↔ medical mapping (SpecificMedicalServiceMedicalService).
-- =======================================================================
CREATE TABLE IF NOT EXISTS specific_medical_service_medical_service (
  specific_medical_service_id              UUID NOT NULL,
  medical_service_id                       UUID NOT NULL,
  is_specific_medical_service_main_belong_to BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (specific_medical_service_id, medical_service_id),
  CONSTRAINT fk_sms_ms__sms FOREIGN KEY (specific_medical_service_id)
    REFERENCES specific_medical_service (id) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_sms_ms__ms FOREIGN KEY (medical_service_id)
    REFERENCES medical_service (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_specific_medical_service_main_belong
  ON specific_medical_service_medical_service (specific_medical_service_id)
  WHERE is_specific_medical_service_main_belong_to = TRUE;

-- ==============================================
-- Health check packages (HealthCheckPackage entity).
-- ==============================================
CREATE TABLE IF NOT EXISTS health_check_package (
  id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name                 VARCHAR(255) NOT NULL,
  is_managed_by_doctor BOOLEAN NOT NULL DEFAULT FALSE,
  managing_doctor_id   VARCHAR(255),
  image                VARCHAR(255),
  package_type_id      UUID NOT NULL,
  package_detail_info  TEXT,
  short_package_info   TEXT,
  slug                 VARCHAR(255) NOT NULL,
  status               approval_status NOT NULL DEFAULT 'DRAFT',
  rejected_reason      TEXT,
  submitted_at         TIMESTAMPTZ,
  approved_at          TIMESTAMPTZ,
  is_deleted           BOOLEAN NOT NULL DEFAULT FALSE,
  created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_hcp_package_type FOREIGN KEY (package_type_id)
    REFERENCES package_type (id) ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_health_check_package_slug
  ON health_check_package (slug);
CREATE INDEX IF NOT EXISTS ix_health_check_package_status
  ON health_check_package (status);
CREATE INDEX IF NOT EXISTS ix_health_check_package_package_type
  ON health_check_package (package_type_id);

-- =======================================================================
-- Specific-medical ↔ health check mapping (SpecificMedicalServiceHealthCheckPackage).
-- =======================================================================
CREATE TABLE IF NOT EXISTS specific_medical_service_health_check_package (
  specific_medical_service_id UUID NOT NULL,
  package_id                  UUID NOT NULL,
  PRIMARY KEY (specific_medical_service_id, package_id),
  CONSTRAINT fk_sms_hcp__sms FOREIGN KEY (specific_medical_service_id)
    REFERENCES specific_medical_service (id) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_sms_hcp__hcp FOREIGN KEY (package_id)
    REFERENCES health_check_package (id) ON UPDATE CASCADE ON DELETE CASCADE
);

-- ======================================================
-- Package ↔ specialty mapping (no FK constraint).
-- ======================================================
CREATE TABLE IF NOT EXISTS health_check_package_specialty (
  package_id   UUID NOT NULL,
  specialty_id UUID NOT NULL,
  PRIMARY KEY (package_id, specialty_id)
);

COMMENT ON TABLE health_check_package_specialty
  IS 'Mapping between health check packages and specialty codes (no FK).';

-- ================================================
-- Public view exposing approved packages only.
-- ================================================
CREATE OR REPLACE VIEW v_public_packages AS
SELECT
  p.id,
  p.name,
  p.image,
  p.slug,
  p.short_package_info,
  p.package_detail_info,
  p.package_type_id,
  p.created_at,
  p.updated_at
FROM health_check_package p
WHERE p.status = 'APPROVED'
  AND p.is_deleted = FALSE;
