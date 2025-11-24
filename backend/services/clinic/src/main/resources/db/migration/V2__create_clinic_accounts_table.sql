CREATE TABLE IF NOT EXISTS clinic_accounts (
  id          VARCHAR(36) PRIMARY KEY,
  clinic_id   VARCHAR(36) NOT NULL,
  account_id  VARCHAR(36) NOT NULL, -- ID from Account/Identity Service
  is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,

  CONSTRAINT fk_clinic_account_clinic
    FOREIGN KEY (clinic_id) REFERENCES clinics (id)
    ON UPDATE CASCADE ON DELETE CASCADE,

  -- Ensure an account is not mapped to the same clinic twice
  CONSTRAINT uq_clinic_account_mapping UNIQUE (clinic_id, account_id)
);

-- Index for finding all accounts belonging to a clinic
CREATE INDEX IF NOT EXISTS idx_clinic_accounts_clinic_id
  ON clinic_accounts (clinic_id);

-- CRITICAL INDEX: For finding which clinic an account belongs to (Your Dashboard use case)
CREATE INDEX IF NOT EXISTS idx_clinic_accounts_account_id
  ON clinic_accounts (account_id);