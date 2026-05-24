-- Repair business_leads text columns if they were created as bytea (legacy import / manual DDL).

DO $$
DECLARE
    col record;
    target_type text;
BEGIN
    FOR col IN
        SELECT column_name
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'business_leads'
          AND data_type = 'bytea'
          AND column_name IN (
              'lead_code', 'business_name', 'owner_name', 'mobile', 'mobile_normalized',
              'alternate_mobile', 'business_type', 'gstin', 'address', 'city',
              'state_code', 'pincode', 'lead_source', 'lead_status', 'priority',
              'remarks', 'external_merchant_id', 'external_shop_id'
          )
    LOOP
        target_type := CASE
            WHEN col.column_name IN ('address', 'remarks') THEN 'TEXT'
            WHEN col.column_name = 'business_name' THEN 'VARCHAR(300)'
            WHEN col.column_name = 'owner_name' THEN 'VARCHAR(200)'
            WHEN col.column_name IN ('mobile', 'alternate_mobile') THEN 'VARCHAR(20)'
            WHEN col.column_name = 'mobile_normalized' THEN 'VARCHAR(15)'
            WHEN col.column_name = 'lead_code' THEN 'VARCHAR(32)'
            WHEN col.column_name = 'city' THEN 'VARCHAR(100)'
            WHEN col.column_name IN ('state_code', 'pincode') THEN 'VARCHAR(10)'
            WHEN col.column_name = 'business_type' THEN 'VARCHAR(50)'
            WHEN col.column_name = 'gstin' THEN 'VARCHAR(20)'
            WHEN col.column_name = 'lead_source' THEN 'VARCHAR(40)'
            WHEN col.column_name = 'lead_status' THEN 'VARCHAR(30)'
            WHEN col.column_name = 'priority' THEN 'VARCHAR(20)'
            WHEN col.column_name IN ('external_merchant_id', 'external_shop_id') THEN 'VARCHAR(64)'
            ELSE 'VARCHAR(255)'
        END;

        EXECUTE format(
            'ALTER TABLE business_leads ALTER COLUMN %I TYPE %s USING convert_from(%I, ''UTF8'')',
            col.column_name,
            target_type,
            col.column_name
        );
    END LOOP;
END $$;
