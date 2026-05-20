package com.shopmanagement.fieldforceservice.integration;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ShopCreationResult;
import com.shopmanagement.fieldforceservice.exception.ConflictException;
import com.shopmanagement.fieldforceservice.model.BusinessLead;

/**
 * Calls shop-service field-force conversion endpoint when enabled.
 */
@Component
@ConditionalOnProperty(name = "fieldforce.shop-integration.enabled", havingValue = "true")
public class RestShopConversionClient implements ShopConversionClient {

    private final RestClient restClient;

    public RestShopConversionClient(@Value("${fieldforce.shop-integration.base-url:http://shop-service:8080}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ShopCreationResult createMerchantAndShop(
            long tenantId,
            BusinessLead lead,
            String subscriptionPlanCode,
            String merchantPayloadJson,
            String ownerEmail,
            String ownerUsername) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("businessName", lead.getBusinessName());
        body.put("ownerName", lead.getOwnerName());
        body.put("mobile", lead.getMobile());
        body.put("address", lead.getAddress());
        body.put("city", lead.getCity());
        body.put("stateCode", lead.getStateCode());
        body.put("pincode", lead.getPincode());
        body.put("subscriptionPlanCode", subscriptionPlanCode);
        body.put("leadCode", lead.getLeadCode());
        if (merchantPayloadJson != null) {
            body.put("merchantPayloadJson", merchantPayloadJson);
        }
        if (ownerEmail != null && !ownerEmail.isBlank()) {
            body.put("ownerEmail", ownerEmail.trim());
        }
        if (ownerUsername != null && !ownerUsername.isBlank()) {
            body.put("ownerUsername", ownerUsername.trim());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Tenant-Id", String.valueOf(tenantId));

        try {
            Map<String, Object> response = restClient
                    .post()
                    .uri("/api/v1/internal/fieldforce/conversions")
                    .headers(h -> h.addAll(headers))
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            if (response == null) {
                throw new ConflictException("Shop service returned empty response");
            }
            return new ShopCreationResult(
                    String.valueOf(response.get("merchantId")),
                    String.valueOf(response.get("shopId")),
                    response.get("ownerInviteEmailQueued") instanceof Boolean b ? b : null,
                    response.get("inviteExpiresAt") != null ? String.valueOf(response.get("inviteExpiresAt")) : null);
        } catch (Exception ex) {
            throw new ConflictException("Shop conversion failed: " + ex.getMessage());
        }
    }
}
