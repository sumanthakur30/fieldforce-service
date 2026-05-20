-- Align performance_targets with TenantAuditableEntity (soft delete).
ALTER TABLE performance_targets
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
