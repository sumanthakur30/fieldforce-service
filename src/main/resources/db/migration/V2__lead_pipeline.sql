-- Lead → Conversion pipeline (enterprise field force CRM)

-- ---------------------------------------------------------------------------
-- business_leads
-- ---------------------------------------------------------------------------
CREATE TABLE business_leads (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    lead_code VARCHAR(32) NOT NULL,
    business_name VARCHAR(300) NOT NULL,
    owner_name VARCHAR(200),
    mobile VARCHAR(20) NOT NULL,
    mobile_normalized VARCHAR(15) NOT NULL,
    alternate_mobile VARCHAR(20),
    business_type VARCHAR(50),
    gstin VARCHAR(20),
    address TEXT,
    city VARCHAR(100),
    state_code VARCHAR(10),
    pincode VARCHAR(10),
    gps_latitude NUMERIC(10, 7),
    gps_longitude NUMERIC(10, 7),
    lead_source VARCHAR(40) NOT NULL DEFAULT 'FIELD_VISIT',
    created_by_promoter_id BIGINT REFERENCES promoters (id),
    assigned_salesman_id BIGINT REFERENCES salesmen (id),
    lead_status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    remarks TEXT,
    expected_conversion_date DATE,
    converted_at TIMESTAMPTZ,
    external_merchant_id VARCHAR(64),
    external_shop_id VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uk_leads_tenant_code UNIQUE (tenant_id, lead_code)
);

CREATE INDEX idx_leads_tenant_status ON business_leads (tenant_id, lead_status) WHERE deleted_at IS NULL;
CREATE INDEX idx_leads_tenant_mobile ON business_leads (tenant_id, mobile_normalized) WHERE deleted_at IS NULL;
CREATE INDEX idx_leads_tenant_salesman ON business_leads (tenant_id, assigned_salesman_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_leads_tenant_promoter ON business_leads (tenant_id, created_by_promoter_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_leads_tenant_city ON business_leads (tenant_id, state_code, city) WHERE deleted_at IS NULL;
CREATE INDEX idx_leads_gstin ON business_leads (tenant_id, gstin) WHERE gstin IS NOT NULL AND deleted_at IS NULL;

-- ---------------------------------------------------------------------------
-- field_activities
-- ---------------------------------------------------------------------------
CREATE TABLE field_activities (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    business_lead_id BIGINT NOT NULL REFERENCES business_leads (id),
    salesman_id BIGINT REFERENCES salesmen (id),
    promoter_id BIGINT REFERENCES promoters (id),
    activity_type VARCHAR(30) NOT NULL,
    activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    gps_latitude NUMERIC(10, 7),
    gps_longitude NUMERIC(10, 7),
    notes TEXT,
    next_followup_date DATE,
    photo_url VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_activities_lead ON field_activities (business_lead_id, activity_at DESC);
CREATE INDEX idx_activities_tenant_at ON field_activities (tenant_id, activity_at DESC);

-- ---------------------------------------------------------------------------
-- beat_plans & assignments
-- ---------------------------------------------------------------------------
CREATE TABLE beat_plans (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    beat_code VARCHAR(32) NOT NULL,
    beat_name VARCHAR(200) NOT NULL,
    state_code VARCHAR(10),
    city_code VARCHAR(100),
    area_description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uk_beat_tenant_code UNIQUE (tenant_id, beat_code)
);

CREATE TABLE salesman_beat_assignments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    beat_plan_id BIGINT NOT NULL REFERENCES beat_plans (id),
    salesman_id BIGINT NOT NULL REFERENCES salesmen (id),
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_beat_salesman_day UNIQUE (tenant_id, salesman_id, day_of_week, beat_plan_id)
);

CREATE INDEX idx_beat_assign_salesman ON salesman_beat_assignments (salesman_id, day_of_week);

-- ---------------------------------------------------------------------------
-- attendance & GPS
-- ---------------------------------------------------------------------------
CREATE TABLE attendance_records (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    salesman_id BIGINT NOT NULL REFERENCES salesmen (id),
    attendance_date DATE NOT NULL,
    check_in_at TIMESTAMPTZ,
    check_out_at TIMESTAMPTZ,
    check_in_latitude NUMERIC(10, 7),
    check_in_longitude NUMERIC(10, 7),
    check_out_latitude NUMERIC(10, 7),
    check_out_longitude NUMERIC(10, 7),
    status VARCHAR(20) NOT NULL DEFAULT 'CHECKED_IN',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_attendance_salesman_day UNIQUE (tenant_id, salesman_id, attendance_date)
);

CREATE TABLE gps_location_logs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    salesman_id BIGINT NOT NULL REFERENCES salesmen (id),
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    latitude NUMERIC(10, 7) NOT NULL,
    longitude NUMERIC(10, 7) NOT NULL,
    accuracy_meters NUMERIC(8, 2),
    source VARCHAR(30) NOT NULL DEFAULT 'LIVE_TRACK',
    business_lead_id BIGINT REFERENCES business_leads (id),
    field_activity_id BIGINT REFERENCES field_activities (id)
);

CREATE INDEX idx_gps_salesman_time ON gps_location_logs (tenant_id, salesman_id, recorded_at DESC);

-- ---------------------------------------------------------------------------
-- performance targets
-- ---------------------------------------------------------------------------
CREATE TABLE performance_targets (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    target_month DATE NOT NULL,
    promoter_id BIGINT REFERENCES promoters (id),
    salesman_id BIGINT REFERENCES salesmen (id),
    lead_target INTEGER NOT NULL DEFAULT 0,
    conversion_target INTEGER NOT NULL DEFAULT 0,
    revenue_target NUMERIC(14, 2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_target_month_person UNIQUE (tenant_id, target_month, promoter_id, salesman_id)
);

-- ---------------------------------------------------------------------------
-- lead conversion pipeline
-- ---------------------------------------------------------------------------
CREATE TABLE lead_conversions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    business_lead_id BIGINT NOT NULL REFERENCES business_leads (id),
    current_stage VARCHAR(40) NOT NULL DEFAULT 'VERIFICATION',
    subscription_plan_code VARCHAR(64),
    kyc_verified BOOLEAN NOT NULL DEFAULT FALSE,
    merchant_payload_json TEXT,
    external_merchant_id VARCHAR(64),
    external_shop_id VARCHAR(64),
    failure_reason TEXT,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_conversion_lead UNIQUE (business_lead_id)
);

-- ---------------------------------------------------------------------------
-- commission extensions
-- ---------------------------------------------------------------------------
ALTER TABLE commission_plans
    ADD COLUMN IF NOT EXISTS lead_created_amount NUMERIC(14, 2),
    ADD COLUMN IF NOT EXISTS demo_completed_amount NUMERIC(14, 2),
    ADD COLUMN IF NOT EXISTS conversion_amount NUMERIC(14, 2);

ALTER TABLE commission_entries
    ALTER COLUMN shop_registration_id DROP NOT NULL;

ALTER TABLE commission_entries
    ADD COLUMN IF NOT EXISTS business_lead_id BIGINT REFERENCES business_leads (id),
    ADD COLUMN IF NOT EXISTS commission_event VARCHAR(40) NOT NULL DEFAULT 'LEGACY_SHOP_APPROVAL';

CREATE INDEX idx_commission_entries_lead ON commission_entries (business_lead_id);
