package com.shopmanagement.fieldforceservice.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.DuplicateCheckRequest;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.DuplicateCheckResult;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.LeadResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.LeadStatusPatch;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.LeadUpsert;
import com.shopmanagement.fieldforceservice.model.LeadStatus;
import com.shopmanagement.fieldforceservice.service.lead.BusinessLeadService;
import com.shopmanagement.fieldforceservice.service.lead.DuplicateLeadDetectionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/leads")
public class BusinessLeadController {

    private final BusinessLeadService leadService;
    private final DuplicateLeadDetectionService duplicateDetection;

    public BusinessLeadController(BusinessLeadService leadService, DuplicateLeadDetectionService duplicateDetection) {
        this.leadService = leadService;
        this.duplicateDetection = duplicateDetection;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeadResponse create(@Valid @RequestBody LeadUpsert body) {
        return leadService.create(body, false);
    }

    @PutMapping("/{id}")
    public LeadResponse update(@PathVariable Long id, @Valid @RequestBody LeadUpsert body) {
        return leadService.update(id, body);
    }

    @PatchMapping("/{id}/status")
    public LeadResponse patchStatus(@PathVariable Long id, @Valid @RequestBody LeadStatusPatch body) {
        return leadService.patchStatus(id, body);
    }

    @GetMapping("/{id}")
    public LeadResponse get(@PathVariable Long id) {
        return leadService.get(id);
    }

    @GetMapping
    public Page<LeadResponse> list(
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) Long promoterId,
            @RequestParam(required = false) Long salesmanId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        return leadService.search(status, promoterId, salesmanId, state, city, q, pageable);
    }

    @PostMapping("/duplicate-check")
    public DuplicateCheckResult duplicateCheck(@Valid @RequestBody DuplicateCheckRequest body) {
        return duplicateDetection.check(body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        leadService.softDelete(id);
    }
}
