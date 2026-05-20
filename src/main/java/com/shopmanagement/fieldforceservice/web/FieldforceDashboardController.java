package com.shopmanagement.fieldforceservice.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.fieldforceservice.api.FieldforceApi.DashboardSummary;
import com.shopmanagement.fieldforceservice.service.DashboardService;

@RestController
@RequestMapping("/api/v1/fieldforce/dashboard")
public class FieldforceDashboardController {

    private final DashboardService dashboardService;

    public FieldforceDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummary summary() {
        return dashboardService.summary();
    }
}
