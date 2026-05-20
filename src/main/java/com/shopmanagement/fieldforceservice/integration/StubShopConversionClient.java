package com.shopmanagement.fieldforceservice.integration;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ShopCreationResult;
import com.shopmanagement.fieldforceservice.model.BusinessLead;

/**
 * Default until shop-service integration is wired. Produces deterministic external IDs for dev/test.
 */
@Component
@ConditionalOnProperty(name = "fieldforce.shop-integration.enabled", havingValue = "false", matchIfMissing = true)
public class StubShopConversionClient implements ShopConversionClient {

    @Override
    public ShopCreationResult createMerchantAndShop(
            long tenantId,
            BusinessLead lead,
            String subscriptionPlanCode,
            String merchantPayloadJson,
            String ownerEmail,
            String ownerUsername) {
        String suffix = lead.getId() != null ? String.valueOf(lead.getId()) : UUID.randomUUID().toString().substring(0, 8);
        return new ShopCreationResult("MCH-" + tenantId + "-" + suffix, "SHP-" + tenantId + "-" + suffix, false, null);
    }
}
