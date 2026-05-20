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

import com.shopmanagement.fieldforceservice.api.FieldforceApi.SalesmanResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.SalesmanUpsert;
import com.shopmanagement.fieldforceservice.model.FieldPersonStatus;
import com.shopmanagement.fieldforceservice.service.SalesmanService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/salesmen")
public class SalesmanController {

    private final SalesmanService salesmanService;

    public SalesmanController(SalesmanService salesmanService) {
        this.salesmanService = salesmanService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalesmanResponse create(@Valid @RequestBody SalesmanUpsert body) {
        return salesmanService.create(body);
    }

    @PutMapping("/{id}")
    public SalesmanResponse update(@PathVariable Long id, @Valid @RequestBody SalesmanUpsert body) {
        return salesmanService.update(id, body);
    }

    @PatchMapping("/{id}/status")
    public SalesmanResponse status(@PathVariable Long id, @RequestParam FieldPersonStatus status) {
        return salesmanService.setStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        salesmanService.delete(id);
    }

    @GetMapping("/{id}")
    public SalesmanResponse get(@PathVariable Long id) {
        return salesmanService.get(id);
    }

    @GetMapping
    public Page<SalesmanResponse> search(
            @RequestParam(required = false) Long promoterId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) FieldPersonStatus status,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return salesmanService.search(promoterId, state, city, status, q, pageable);
    }
}
