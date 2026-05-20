package com.shopmanagement.fieldforceservice.web;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ActivityCreate;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ActivityResponse;
import com.shopmanagement.fieldforceservice.model.FieldActivityType;
import com.shopmanagement.fieldforceservice.service.activity.FieldActivityService;

import jakarta.validation.Valid;

@RestController
public class FieldActivityController {

    private final FieldActivityService activityService;

    public FieldActivityController(FieldActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/api/v1/leads/{leadId}/activities")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityResponse create(@PathVariable Long leadId, @Valid @RequestBody ActivityCreate body) {
        return activityService.log(leadId, body);
    }

    @GetMapping("/api/v1/leads/{leadId}/activities")
    public List<ActivityResponse> listForLead(@PathVariable Long leadId) {
        return activityService.listForLead(leadId);
    }

    @GetMapping("/api/v1/activities")
    public Page<ActivityResponse> list(
            @RequestParam(required = false) Long leadId,
            @RequestParam(required = false) Long salesmanId,
            @RequestParam(required = false) FieldActivityType type,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            Pageable pageable) {
        return activityService.filter(leadId, salesmanId, type, from, to, pageable);
    }
}
