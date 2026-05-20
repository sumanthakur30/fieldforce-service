package com.shopmanagement.fieldforceservice.integration;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ShopCreationResult;
import com.shopmanagement.fieldforceservice.model.BusinessLead;

/**
 * Creates merchant + shop in shop-service after lead conversion. fieldforce-service must never
 * insert shop rows directly.
 */
public interface ShopConversionClient {

    ShopCreationResult createMerchantAndShop(
            long tenantId,
            BusinessLead lead,
            String subscriptionPlanCode,
            String merchantPayloadJson,
            String ownerEmail,
            String ownerUsername);
}
