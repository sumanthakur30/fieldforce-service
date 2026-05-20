-- Lead-pipeline commission plan amounts (V2); safe if columns already exist.

ALTER TABLE commission_plans
    ADD COLUMN IF NOT EXISTS lead_created_amount NUMERIC(14, 2),
    ADD COLUMN IF NOT EXISTS demo_completed_amount NUMERIC(14, 2),
    ADD COLUMN IF NOT EXISTS conversion_amount NUMERIC(14, 2);
