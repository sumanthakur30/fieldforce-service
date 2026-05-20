-- Repair RDS fieldforcedb when V2 was skipped or partially applied (commission-entries API 500).

ALTER TABLE commission_entries
    ALTER COLUMN shop_registration_id DROP NOT NULL;

ALTER TABLE commission_entries
    ADD COLUMN IF NOT EXISTS business_lead_id BIGINT;

ALTER TABLE commission_entries
    ADD COLUMN IF NOT EXISTS commission_event VARCHAR(40);

UPDATE commission_entries
SET commission_event = 'LEGACY_SHOP_APPROVAL'
WHERE commission_event IS NULL;

ALTER TABLE commission_entries
    ALTER COLUMN commission_event SET NOT NULL;

ALTER TABLE commission_entries
    ALTER COLUMN commission_event SET DEFAULT 'LEGACY_SHOP_APPROVAL';

CREATE INDEX IF NOT EXISTS idx_commission_entries_lead ON commission_entries (business_lead_id);
