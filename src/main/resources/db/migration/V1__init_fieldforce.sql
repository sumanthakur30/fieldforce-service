-- Field force: promoters, territories, salesmen, shop attribution, commission MVP

CREATE TABLE promoters (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    promoter_code VARCHAR(32) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    mobile VARCHAR(20) NOT NULL,
    email VARCHAR(200),
    address TEXT,
    state_code VARCHAR(10) NOT NULL,
    city_code VARCHAR(100) NOT NULL,
    profile_photo_url VARCHAR(500),
    joining_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    pan VARCHAR(20),
    aadhaar_masked VARCHAR(20),
    gstin VARCHAR(20),
    bank_account_name VARCHAR(200),
    bank_ifsc VARCHAR(20),
    bank_account_number VARCHAR(40),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_promoters_tenant_code UNIQUE (tenant_id, promoter_code)
);

CREATE INDEX idx_promoters_tenant_state_city ON promoters (tenant_id, state_code, city_code);
CREATE INDEX idx_promoters_tenant_status ON promoters (tenant_id, status);

CREATE TABLE promoter_territories (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    promoter_id BIGINT NOT NULL REFERENCES promoters (id) ON DELETE CASCADE,
    state_code VARCHAR(10) NOT NULL,
    city_code VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_territory_city_exclusive UNIQUE (tenant_id, state_code, city_code)
);

CREATE INDEX idx_promoter_territories_promoter ON promoter_territories (promoter_id);

CREATE TABLE salesmen (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    salesman_code VARCHAR(32) NOT NULL,
    promoter_id BIGINT NOT NULL REFERENCES promoters (id) ON DELETE CASCADE,
    full_name VARCHAR(200) NOT NULL,
    mobile VARCHAR(20) NOT NULL,
    email VARCHAR(200),
    state_code VARCHAR(10) NOT NULL,
    city_code VARCHAR(100) NOT NULL,
    joining_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_salesmen_tenant_code UNIQUE (tenant_id, salesman_code)
);

CREATE INDEX idx_salesmen_promoter ON salesmen (promoter_id);
CREATE INDEX idx_salesmen_tenant_state_city ON salesmen (tenant_id, state_code, city_code);

CREATE TABLE shop_registrations (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    external_shop_id VARCHAR(64) NOT NULL,
    shop_name VARCHAR(300) NOT NULL,
    owner_name VARCHAR(200),
    owner_mobile VARCHAR(20),
    address TEXT,
    state_code VARCHAR(10),
    city_code VARCHAR(100),
    registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    promoter_id BIGINT REFERENCES promoters (id),
    salesman_id BIGINT REFERENCES salesmen (id),
    approval_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_shop_ext UNIQUE (tenant_id, external_shop_id)
);

CREATE INDEX idx_shop_reg_promoter ON shop_registrations (promoter_id);
CREATE INDEX idx_shop_reg_salesman ON shop_registrations (salesman_id);
CREATE INDEX idx_shop_reg_state_city ON shop_registrations (tenant_id, state_code, city_code);
CREATE INDEX idx_shop_reg_registered_at ON shop_registrations (tenant_id, registered_at);

CREATE TABLE commission_plans (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    promoter_fixed_amount NUMERIC(14, 2),
    promoter_percent NUMERIC(7, 4),
    salesman_fixed_amount NUMERIC(14, 2),
    salesman_percent NUMERIC(7, 4),
    effective_from DATE NOT NULL,
    effective_to DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_commission_plans_tenant_active ON commission_plans (tenant_id, active);

CREATE TABLE commission_entries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_registration_id BIGINT NOT NULL REFERENCES shop_registrations (id),
    beneficiary_type VARCHAR(20) NOT NULL,
    beneficiary_id BIGINT NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    period_month DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    paid_at TIMESTAMPTZ,
    payout_reference VARCHAR(120)
);

CREATE INDEX idx_commission_entries_shop ON commission_entries (shop_registration_id);
CREATE INDEX idx_commission_entries_beneficiary ON commission_entries (tenant_id, beneficiary_type, beneficiary_id, status);
