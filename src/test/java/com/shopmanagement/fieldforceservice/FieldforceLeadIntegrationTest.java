package com.shopmanagement.fieldforceservice;

import static org.assertj.core.api.Assertions.assertThat;

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
class FieldforceLeadIntegrationTest {

    private static final long TENANT_ID = 515151L;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void leadActivityConversionHappyPath() throws Exception {
        HttpHeaders headers = tenantHeaders();

        long promoterId = createPromoter(headers);
        long salesmanId = createSalesman(headers, promoterId);

        Map<String, Object> lead = new LinkedHashMap<>();
        lead.put("businessName", "Lead Store " + UUID.randomUUID().toString().substring(0, 6));
        lead.put("ownerName", "Owner");
        lead.put("mobile", "9811111111");
        lead.put("leadSource", "FIELD_VISIT");
        lead.put("createdByPromoterId", promoterId);
        lead.put("assignedSalesmanId", salesmanId);
        lead.put("priority", "HIGH");
        lead.put("stateCode", "DL");
        lead.put("city", "Delhi");

        ResponseEntity<String> leadRes = restTemplate.exchange(
                "/api/v1/leads", HttpMethod.POST, new HttpEntity<>(lead, headers), String.class);
        assertThat(leadRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode leadJson = objectMapper.readTree(leadRes.getBody());
        long leadId = leadJson.get("id").asLong();
        assertThat(leadJson.get("leadCode").asText()).startsWith("LD-");

        Map<String, Object> activity = Map.of("activityType", "DEMO", "notes", "Product demo");
        ResponseEntity<String> actRes = restTemplate.exchange(
                "/api/v1/leads/" + leadId + "/activities",
                HttpMethod.POST,
                new HttpEntity<>(activity, headers),
                String.class);
        assertThat(actRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Map<String, Object> conversion = Map.of("stage", "KYC", "kycVerified", true);
        restTemplate.exchange(
                "/api/v1/leads/" + leadId + "/conversion",
                HttpMethod.POST,
                new HttpEntity<>(conversion, headers),
                String.class);

        ResponseEntity<String> completeRes = restTemplate.exchange(
                "/api/v1/leads/" + leadId + "/conversion/complete",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("subscriptionPlanCode", "BASIC"), headers),
                String.class);
        assertThat(completeRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode conv = objectMapper.readTree(completeRes.getBody());
        assertThat(conv.get("currentStage").asText()).isEqualTo("COMPLETED");
        assertThat(conv.get("externalShopId").asText()).isNotBlank();

        ResponseEntity<String> funnelRes = restTemplate.exchange(
                "/api/v1/fieldforce/analytics/funnel",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertThat(funnelRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(funnelRes.getBody()).contains("CONVERTED");
    }

    private long createPromoter(HttpHeaders headers) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fullName", "Lead Promoter");
        body.put("mobile", "9900" + (System.currentTimeMillis() % 100000));
        body.put("stateCode", "DL");
        body.put("cityCode", "DEL");
        body.put("joiningDate", LocalDate.now().toString());
        ResponseEntity<String> res = restTemplate.exchange(
                "/api/v1/promoters", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        return objectMapper.readTree(res.getBody()).get("id").asLong();
    }

    private long createSalesman(HttpHeaders headers, long promoterId) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("promoterId", promoterId);
        body.put("fullName", "Lead Salesman");
        body.put("mobile", "9911" + (System.currentTimeMillis() % 100000));
        body.put("stateCode", "DL");
        body.put("cityCode", "DEL");
        body.put("joiningDate", LocalDate.now().toString());
        ResponseEntity<String> res = restTemplate.exchange(
                "/api/v1/salesmen", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        return objectMapper.readTree(res.getBody()).get("id").asLong();
    }

    private static HttpHeaders tenantHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set(RequestIdFilter.TENANT_ID_HEADER, Long.toString(TENANT_ID));
        h.set(RequestIdFilter.REQUEST_ID_HEADER, UUID.randomUUID().toString());
        return h;
    }
}
