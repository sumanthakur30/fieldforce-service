package com.shopmanagement.fieldforceservice.support;

import com.shopmanagement.fieldforceservice.filter.RequestIdFilter;

public final class TenantIds {

    private TenantIds() {
    }

    public static long require() {
        Long tenantId = RequestIdFilter.getCurrentTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Missing tenant context");
        }
        return tenantId;
    }
}
