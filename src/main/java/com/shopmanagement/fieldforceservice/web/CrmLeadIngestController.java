package com.shopmanagement.fieldforceservice.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.LeadResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.LeadUpsert;
import com.shopmanagement.fieldforceservice.model.LeadPriority;
import com.shopmanagement.fieldforceservice.model.LeadSource;
import com.shopmanagement.fieldforceservice.service.lead.BusinessLeadService;

/**
 * CRM LeadConvertService target FIELD_FORCE → {@code POST /api/v1/leads/from-crm}.
 */
@RestController
@RequestMapping("/api/v1/leads")
public class CrmLeadIngestController {

  private final BusinessLeadService leadService;

  public CrmLeadIngestController(BusinessLeadService leadService) {
    this.leadService = leadService;
  }

  @PostMapping("/from-crm")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> fromCrm(@RequestBody Map<String, Object> body) {
    String businessName = first(body, "companyName", "title", "displayName");
    if (businessName == null || businessName.isBlank()) {
      businessName = "CRM lead " + body.get("crmLeadId");
    }
    String mobile = first(body, "phone");
    if (mobile == null || mobile.isBlank()) {
      mobile = "0000000000";
    }
    LeadUpsert upsert =
        new LeadUpsert(
            businessName,
            first(body, "displayName"),
            mobile,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            LeadSource.OTHER,
            null,
            null,
            LeadPriority.MEDIUM,
            "from CRM lead " + body.get("crmLeadId") + " corr=" + body.get("correlationId"),
            null);
    LeadResponse created = leadService.create(upsert, false);
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("id", created.id());
    response.put("externalId", created.id());
    response.put("leadCode", created.leadCode());
    response.put("status", "ACCEPTED");
    response.put("crmLeadId", body.get("crmLeadId"));
    return response;
  }

  private static String first(Map<String, Object> body, String... keys) {
    for (String k : keys) {
      Object v = body.get(k);
      if (v != null && !String.valueOf(v).isBlank()) {
        return String.valueOf(v).trim();
      }
    }
    return null;
  }
}
