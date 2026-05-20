package com.shopmanagement.fieldforceservice.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ConversionCompleteRequest;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ConversionResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ConversionUpsert;
import com.shopmanagement.fieldforceservice.service.conversion.LeadConversionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/leads/{leadId}/conversion")
public class LeadConversionController {

    private final LeadConversionService conversionService;

    public LeadConversionController(LeadConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversionResponse upsert(@PathVariable Long leadId, @Valid @RequestBody ConversionUpsert body) {
        return conversionService.startOrUpdate(leadId, body);
    }

    @PostMapping("/complete")
    public ConversionResponse complete(
            @PathVariable Long leadId, @RequestBody(required = false) ConversionCompleteRequest body) {
        return conversionService.complete(leadId, body);
    }

    @GetMapping
    public ConversionResponse get(@PathVariable Long leadId) {
        return conversionService.get(leadId);
    }
}
