package com.shopmanagement.fieldforceservice.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.fieldforceservice.api.FieldforceApi.CommissionPlanResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.CommissionPlanUpsert;
import com.shopmanagement.fieldforceservice.service.CommissionPlanService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/commission-plans")
public class CommissionPlanController {

    private final CommissionPlanService commissionPlanService;

    public CommissionPlanController(CommissionPlanService commissionPlanService) {
        this.commissionPlanService = commissionPlanService;
    }

    @GetMapping
    public List<CommissionPlanResponse> list() {
        return commissionPlanService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommissionPlanResponse create(@Valid @RequestBody CommissionPlanUpsert body) {
        return commissionPlanService.create(body);
    }

    @PutMapping("/{id}")
    public CommissionPlanResponse update(@PathVariable Long id, @Valid @RequestBody CommissionPlanUpsert body) {
        return commissionPlanService.update(id, body);
    }
}
