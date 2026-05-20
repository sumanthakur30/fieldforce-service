package com.shopmanagement.fieldforceservice.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.fieldforceservice.api.FieldforceApi.CommissionEntryResponse;
import com.shopmanagement.fieldforceservice.model.CommissionBeneficiaryType;
import com.shopmanagement.fieldforceservice.model.CommissionEntryStatus;
import com.shopmanagement.fieldforceservice.service.CommissionEntryService;

@RestController
@RequestMapping("/api/v1/commission-entries")
public class CommissionEntryController {

    private final CommissionEntryService commissionEntryService;

    public CommissionEntryController(CommissionEntryService commissionEntryService) {
        this.commissionEntryService = commissionEntryService;
    }

    @GetMapping
    public Page<CommissionEntryResponse> search(
            @RequestParam(required = false) CommissionEntryStatus status,
            @RequestParam(required = false) Long shopRegistrationId,
            @RequestParam(required = false) CommissionBeneficiaryType beneficiaryType,
            @RequestParam(required = false) Long beneficiaryId,
            @PageableDefault(size = 20) Pageable pageable) {
        return commissionEntryService.search(status, shopRegistrationId, beneficiaryType, beneficiaryId, pageable);
    }
}
