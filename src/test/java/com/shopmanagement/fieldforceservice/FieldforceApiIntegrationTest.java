package com.shopmanagement.fieldforceservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmanagement.fieldforceservice.filter.RequestIdFilter;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FieldforceApiIntegrationTest {

    private static final long TENANT_ID = 424242L;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void actuatorHealthDoesNotRequireTenant() {
        ResponseEntity<String> r =
                restTemplate.getForEntity("/actuator/health", String.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void apiRequiresXTenantId() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/v1/promoters",
                HttpMethod.GET,
                new HttpEntity<>(h),
                String.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r.getBody()).contains("X-Tenant-Id");
    }

    @Test
    void promoterTerritorySalesmanShopCommissionHappyPath() throws Exception {
        HttpHeaders headers = tenantHeaders();

        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("name", "Integration plan");
        plan.put("promoterFixedAmount", new BigDecimal("100.00"));
        plan.put("promoterPercent", null);
        plan.put("salesmanFixedAmount", new BigDecimal("40.00"));
        plan.put("salesmanPercent", null);
        plan.put("effectiveFrom", "2020-01-01");
        plan.put("effectiveTo", null);
        plan.put("active", true);

        ResponseEntity<String> planRes = restTemplate.exchange(
                "/api/v1/commission-plans",
                HttpMethod.POST,
                new HttpEntity<>(plan, headers),
                String.class);
        assertThat(planRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        LocalDate joining = LocalDate.of(2026, 1, 10);
        Map<String, Object> promoterBody = new LinkedHashMap<>();
        promoterBody.put("fullName", "Integration Promoter");
        promoterBody.put("mobile", "9800000001");
        promoterBody.put("email", "p@example.com");
        promoterBody.put("address", "Addr");
        promoterBody.put("stateCode", "KA");
        promoterBody.put("cityCode", "BLR");
        promoterBody.put("profilePhotoUrl", null);
        promoterBody.put("joiningDate", joining.toString());
        promoterBody.put("pan", null);
        promoterBody.put("aadhaarMasked", null);
        promoterBody.put("gstin", null);
        promoterBody.put("bankAccountName", null);
        promoterBody.put("bankIfsc", null);
        promoterBody.put("bankAccountNumber", null);

        ResponseEntity<String> promoterRes = restTemplate.exchange(
                "/api/v1/promoters",
                HttpMethod.POST,
                new HttpEntity<>(promoterBody, headers),
                String.class);
        assertThat(promoterRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode promoterJson = objectMapper.readTree(promoterRes.getBody());
        long promoterId = promoterJson.get("id").asLong();
        assertThat(promoterJson.get("promoterCode").asText()).startsWith("PF-");

        Map<String, Object> territoryBody =
                Map.of("stateCode", "KA", "cityCode", "MYS");
        ResponseEntity<String> territoryRes = restTemplate.exchange(
                "/api/v1/promoters/" + promoterId + "/territories",
                HttpMethod.POST,
                new HttpEntity<>(territoryBody, headers),
                String.class);
        assertThat(territoryRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Map<String, Object> salesmanBody = new LinkedHashMap<>();
        salesmanBody.put("promoterId", promoterId);
        salesmanBody.put("fullName", "Integration Salesman");
        salesmanBody.put("mobile", "9800000002");
        salesmanBody.put("email", null);
        salesmanBody.put("stateCode", "KA");
        salesmanBody.put("cityCode", "BLR");
        salesmanBody.put("joiningDate", joining.toString());

        ResponseEntity<String> salesmanRes = restTemplate.exchange(
                "/api/v1/salesmen",
                HttpMethod.POST,
                new HttpEntity<>(salesmanBody, headers),
                String.class);
        assertThat(salesmanRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode salesmanJson = objectMapper.readTree(salesmanRes.getBody());
        long salesmanId = salesmanJson.get("id").asLong();
        assertThat(salesmanJson.get("salesmanCode").asText()).startsWith("SF-");

        String externalShopId = "EXT-" + UUID.randomUUID();
        Map<String, Object> shopBody = new LinkedHashMap<>();
        shopBody.put("externalShopId", externalShopId);
        shopBody.put("shopName", "Test Shop");
        shopBody.put("ownerName", "Owner");
        shopBody.put("ownerMobile", "9100000000");
        shopBody.put("address", "Shop addr");
        shopBody.put("stateCode", "KA");
        shopBody.put("cityCode", "BLR");
        shopBody.put("promoterId", promoterId);
        shopBody.put("salesmanId", salesmanId);
        shopBody.put("registeredAt", null);

        ResponseEntity<String> shopRes = restTemplate.exchange(
                "/api/v1/shop-registrations",
                HttpMethod.POST,
                new HttpEntity<>(shopBody, headers),
                String.class);
        assertThat(shopRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode shopJson = objectMapper.readTree(shopRes.getBody());
        long shopId = shopJson.get("id").asLong();
        assertThat(shopJson.get("approvalStatus").asText()).isEqualTo("PENDING");
        assertThat(shopJson.get("promoterId").asLong()).isEqualTo(promoterId);
        assertThat(shopJson.get("salesmanId").asLong()).isEqualTo(salesmanId);

        ResponseEntity<String> dashPendingRes = restTemplate.exchange(
                "/api/v1/fieldforce/dashboard/summary",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertThat(dashPendingRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(dashPendingRes.getBody()).get("shopsPendingApproval").asLong()).isGreaterThanOrEqualTo(1);

        Map<String, Object> approval = Map.of("approvalStatus", "APPROVED");
        ResponseEntity<String> approveRes = restTemplate.exchange(
                "/api/v1/shop-registrations/" + shopId + "/approval",
                HttpMethod.PATCH,
                new HttpEntity<>(approval, headers),
                String.class);
        assertThat(approveRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(approveRes.getBody()).get("approvalStatus").asText())
                .isEqualTo("APPROVED");

        ResponseEntity<String> entriesRes = restTemplate.exchange(
                "/api/v1/commission-entries?shopRegistrationId=" + shopId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertThat(entriesRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode page = objectMapper.readTree(entriesRes.getBody());
        assertThat(page.get("content").isArray()).isTrue();
        assertThat(page.get("content")).hasSize(2);

        ResponseEntity<String> dashRes = restTemplate.exchange(
                "/api/v1/fieldforce/dashboard/summary",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertThat(dashRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode dash = objectMapper.readTree(dashRes.getBody());
        assertThat(dash.get("promoters").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(dash.get("salesmen").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(dash.get("shops").asLong()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void shopRegistration_withOnlySalesmanId_autoAttachesPromoter() throws Exception {
        HttpHeaders headers = tenantHeaders();
        LocalDate joining = LocalDate.of(2026, 2, 1);
        Map<String, Object> promoterBody = new LinkedHashMap<>();
        promoterBody.put("fullName", "Auto-Link Promoter");
        promoterBody.put("mobile", "9811111111");
        promoterBody.put("email", "auto-p@example.com");
        promoterBody.put("address", "A");
        promoterBody.put("stateCode", "KA");
        promoterBody.put("cityCode", "BLR");
        promoterBody.put("profilePhotoUrl", null);
        promoterBody.put("joiningDate", joining.toString());
        promoterBody.put("pan", null);
        promoterBody.put("aadhaarMasked", null);
        promoterBody.put("gstin", null);
        promoterBody.put("bankAccountName", null);
        promoterBody.put("bankIfsc", null);
        promoterBody.put("bankAccountNumber", null);
        ResponseEntity<String> promoterRes = restTemplate.exchange(
                "/api/v1/promoters",
                HttpMethod.POST,
                new HttpEntity<>(promoterBody, headers),
                String.class);
        assertThat(promoterRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long promoterId = objectMapper.readTree(promoterRes.getBody()).get("id").asLong();

        Map<String, Object> salesmanBody = new LinkedHashMap<>();
        salesmanBody.put("promoterId", promoterId);
        salesmanBody.put("fullName", "Auto-Link Salesman");
        salesmanBody.put("mobile", "9822222222");
        salesmanBody.put("email", null);
        salesmanBody.put("stateCode", "KA");
        salesmanBody.put("cityCode", "BLR");
        salesmanBody.put("joiningDate", joining.toString());
        ResponseEntity<String> salesmanRes = restTemplate.exchange(
                "/api/v1/salesmen",
                HttpMethod.POST,
                new HttpEntity<>(salesmanBody, headers),
                String.class);
        assertThat(salesmanRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long salesmanId = objectMapper.readTree(salesmanRes.getBody()).get("id").asLong();

        String externalShopId = "EXT-SL-" + UUID.randomUUID();
        Map<String, Object> shopBody = new LinkedHashMap<>();
        shopBody.put("externalShopId", externalShopId);
        shopBody.put("shopName", "Salesman-only reg shop");
        shopBody.put("ownerName", "O");
        shopBody.put("ownerMobile", "9100000001");
        shopBody.put("address", null);
        shopBody.put("stateCode", "KA");
        shopBody.put("cityCode", "BLR");
        shopBody.put("promoterId", null);
        shopBody.put("salesmanId", salesmanId);
        shopBody.put("registeredAt", null);

        ResponseEntity<String> shopRes = restTemplate.exchange(
                "/api/v1/shop-registrations",
                HttpMethod.POST,
                new HttpEntity<>(shopBody, headers),
                String.class);
        assertThat(shopRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode shopJson = objectMapper.readTree(shopRes.getBody());
        assertThat(shopJson.get("promoterId").asLong()).isEqualTo(promoterId);
        assertThat(shopJson.get("salesmanId").asLong()).isEqualTo(salesmanId);
        assertThat(shopJson.get("approvalStatus").asText()).isEqualTo("PENDING");

        ResponseEntity<String> dashRes = restTemplate.exchange(
                "/api/v1/fieldforce/dashboard/summary",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertThat(dashRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode dash = objectMapper.readTree(dashRes.getBody());
        assertThat(dash.get("promoters").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(dash.get("salesmen").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(dash.get("shops").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(dash.get("shopsPendingApproval").asLong()).isGreaterThanOrEqualTo(1);

        ResponseEntity<String> listBySalesman = restTemplate.exchange(
                "/api/v1/shop-registrations?salesmanId=" + salesmanId + "&size=50",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertThat(listBySalesman.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode pageBySm = objectMapper.readTree(listBySalesman.getBody());
        assertThat(pageBySm.get("content").isArray()).isTrue();
        assertThat(containsExternalShopId(pageBySm.get("content"), externalShopId, promoterId, salesmanId)).isTrue();

        ResponseEntity<String> listByPromoter = restTemplate.exchange(
                "/api/v1/shop-registrations?promoterId=" + promoterId + "&size=50",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertThat(listByPromoter.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode pageByPr = objectMapper.readTree(listByPromoter.getBody());
        assertThat(containsExternalShopId(pageByPr.get("content"), externalShopId, promoterId, salesmanId)).isTrue();
    }

    private static boolean containsExternalShopId(
            JsonNode content, String externalShopId, long expectedPromoterId, long expectedSalesmanId) {
        if (content == null || !content.isArray()) {
            return false;
        }
        for (JsonNode row : content) {
            if (!row.hasNonNull("externalShopId")) {
                continue;
            }
            if (!externalShopId.equals(row.get("externalShopId").asText())) {
                continue;
            }
            return row.get("promoterId").asLong() == expectedPromoterId
                    && row.get("salesmanId").asLong() == expectedSalesmanId;
        }
        return false;
    }

    private HttpHeaders tenantHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.add(RequestIdFilter.TENANT_ID_HEADER, Long.toString(TENANT_ID));
        return h;
    }
}
