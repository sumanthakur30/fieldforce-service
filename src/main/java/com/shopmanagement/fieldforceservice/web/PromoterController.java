package com.shopmanagement.fieldforceservice.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import com.shopmanagement.fieldforceservice.api.FieldforceApi.PromoterDetailResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.PromoterResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.PromoterUpsert;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.TerritoryResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.TerritoryUpsert;
import com.shopmanagement.fieldforceservice.model.FieldPersonStatus;
import com.shopmanagement.fieldforceservice.service.PromoterService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promoters")
public class PromoterController {

    private final PromoterService promoterService;

    public PromoterController(PromoterService promoterService) {
        this.promoterService = promoterService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromoterResponse create(@Valid @RequestBody PromoterUpsert body) {
        return promoterService.create(body);
    }

    @PutMapping("/{id}")
    public PromoterResponse update(@PathVariable Long id, @Valid @RequestBody PromoterUpsert body) {
        return promoterService.update(id, body);
    }

    @PatchMapping("/{id}/status")
    public PromoterResponse status(@PathVariable Long id, @RequestParam FieldPersonStatus status) {
        return promoterService.setStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        promoterService.delete(id);
    }

    @GetMapping("/{id}")
    public PromoterDetailResponse get(@PathVariable Long id) {
        return promoterService.get(id);
    }

    @GetMapping
    public Page<PromoterResponse> search(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) FieldPersonStatus status,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return promoterService.search(state, city, status, q, pageable);
    }

    @GetMapping("/{id}/territories")
    public List<TerritoryResponse> territories(@PathVariable Long id) {
        return promoterService.listTerritories(id);
    }

    @PostMapping("/{id}/territories")
    @ResponseStatus(HttpStatus.CREATED)
    public TerritoryResponse addTerritory(@PathVariable Long id, @Valid @RequestBody TerritoryUpsert body) {
        return promoterService.addTerritory(id, body);
    }

    @DeleteMapping("/{id}/territories/{territoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTerritory(@PathVariable Long id, @PathVariable Long territoryId) {
        promoterService.deleteTerritory(id, territoryId);
    }
}
