package com.shopmanagement.fieldforceservice.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ExtendedDashboardSummary;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.LeadFunnelAnalytics;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.PerformanceAnalytics;
import com.shopmanagement.fieldforceservice.service.analytics.FieldforceAnalyticsService;

@RestController
@RequestMapping("/api/v1/fieldforce/analytics")
public class FieldforceAnalyticsController {

    private final FieldforceAnalyticsService analyticsService;

    public FieldforceAnalyticsController(FieldforceAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/funnel")
    public LeadFunnelAnalytics funnel() {
        return analyticsService.funnel();
    }

    @GetMapping("/performance")
    public PerformanceAnalytics performance() {
        return analyticsService.performance();
    }

    @GetMapping("/dashboard")
    public ExtendedDashboardSummary dashboard() {
        return analyticsService.extendedSummary();
    }
}
