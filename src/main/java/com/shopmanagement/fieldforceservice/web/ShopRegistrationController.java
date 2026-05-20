package com.shopmanagement.fieldforceservice.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.fieldforceservice.api.FieldforceApi.ApprovalPatch;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.ShopRegistrationResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.ShopRegistrationUpsert;
import com.shopmanagement.fieldforceservice.model.ApprovalStatus;
import com.shopmanagement.fieldforceservice.service.ShopRegistrationService;

import jakarta.validation.Valid;

/**
 * @deprecated Use {@link BusinessLeadController} and {@link LeadConversionController}. Legacy shop
 *     attribution pollutes customer data.
 */
@Deprecated
@RestController
@RequestMapping("/api/v1/shop-registrations")
public class ShopRegistrationController {

    private final ShopRegistrationService shopRegistrationService;

    public ShopRegistrationController(ShopRegistrationService shopRegistrationService) {
        this.shopRegistrationService = shopRegistrationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ShopRegistrationResponse upsert(@Valid @RequestBody ShopRegistrationUpsert body) {
        return shopRegistrationService.upsert(body);
    }

    @PatchMapping("/{id}/approval")
    public ShopRegistrationResponse approval(@PathVariable Long id, @Valid @RequestBody ApprovalPatch body) {
        return shopRegistrationService.patchApproval(id, body);
    }

    @GetMapping("/{id}")
    public ShopRegistrationResponse get(@PathVariable Long id) {
        return shopRegistrationService.get(id);
    }

    @GetMapping
    public Page<ShopRegistrationResponse> list(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) Long promoterId,
            @RequestParam(required = false) Long salesmanId,
            @PageableDefault(size = 20) Pageable pageable) {
        return shopRegistrationService.filter(state, city, status, promoterId, salesmanId, pageable);
    }
}
