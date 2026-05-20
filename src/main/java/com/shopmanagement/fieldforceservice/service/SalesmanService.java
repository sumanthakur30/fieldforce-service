package com.shopmanagement.fieldforceservice.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.fieldforceservice.api.FieldforceApi.SalesmanResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.SalesmanUpsert;
import com.shopmanagement.fieldforceservice.exception.NotFoundException;
import com.shopmanagement.fieldforceservice.model.FieldPersonStatus;
import com.shopmanagement.fieldforceservice.model.Promoter;
import com.shopmanagement.fieldforceservice.model.Salesman;
import com.shopmanagement.fieldforceservice.repository.PromoterRepository;
import com.shopmanagement.fieldforceservice.repository.SalesmanRepository;
import com.shopmanagement.fieldforceservice.support.TenantIds;

@Service
public class SalesmanService {

    private final SalesmanRepository salesmanRepository;
    private final PromoterRepository promoterRepository;

    public SalesmanService(SalesmanRepository salesmanRepository, PromoterRepository promoterRepository) {
        this.salesmanRepository = salesmanRepository;
        this.promoterRepository = promoterRepository;
    }

    @Transactional
    public SalesmanResponse create(SalesmanUpsert r) {
        long tenantId = TenantIds.require();
        Promoter promoter = promoterRepository.findByTenantIdAndId(tenantId, r.promoterId())
                .orElseThrow(() -> new NotFoundException("Promoter not found"));
        Salesman s = new Salesman();
        s.setTenantId(tenantId);
        s.setPromoter(promoter);
        s.setSalesmanCode(tempUniqueCode("TMP-S-"));
        apply(s, r);
        s = salesmanRepository.save(s);
        s.setSalesmanCode("SF-" + s.getId());
        s.setUpdatedAt(Instant.now());
        salesmanRepository.save(s);
        return toResponse(s);
    }

    @Transactional
    public SalesmanResponse update(Long id, SalesmanUpsert r) {
        long tenantId = TenantIds.require();
        Salesman s = load(tenantId, id);
        Promoter promoter = promoterRepository.findByTenantIdAndId(tenantId, r.promoterId())
                .orElseThrow(() -> new NotFoundException("Promoter not found"));
        s.setPromoter(promoter);
        apply(s, r);
        s.setUpdatedAt(Instant.now());
        salesmanRepository.save(s);
        return toResponse(s);
    }

    @Transactional
    public SalesmanResponse setStatus(Long id, FieldPersonStatus status) {
        Salesman s = load(TenantIds.require(), id);
        s.setStatus(status);
        s.setUpdatedAt(Instant.now());
        salesmanRepository.save(s);
        return toResponse(s);
    }

    @Transactional
    public void delete(Long id) {
        salesmanRepository.delete(load(TenantIds.require(), id));
    }

    @Transactional(readOnly = true)
    public SalesmanResponse get(Long id) {
        return toResponse(load(TenantIds.require(), id));
    }

    @Transactional(readOnly = true)
    public Page<SalesmanResponse> search(Long promoterId, String state, String city, FieldPersonStatus status, String q, Pageable pageable) {
        long tenantId = TenantIds.require();
        return salesmanRepository.search(
                        tenantId,
                        promoterId,
                        emptyToNull(state),
                        emptyToNull(city),
                        status,
                        emptyToNull(q),
                        pageable)
                .map(this::toResponse);
    }

    private Salesman load(long tenantId, Long id) {
        return salesmanRepository.findByTenantIdAndId(tenantId, id).orElseThrow(() -> new NotFoundException("Salesman not found"));
    }

    private void apply(Salesman s, SalesmanUpsert r) {
        s.setFullName(r.fullName().trim());
        s.setMobile(r.mobile().trim());
        s.setEmail(trimNull(r.email()));
        s.setStateCode(r.stateCode().trim());
        s.setCityCode(r.cityCode().trim());
        s.setJoiningDate(r.joiningDate());
    }

    private SalesmanResponse toResponse(Salesman s) {
        return new SalesmanResponse(
                s.getId(),
                s.getSalesmanCode(),
                s.getPromoter().getId(),
                s.getPromoter().getPromoterCode(),
                s.getFullName(),
                s.getMobile(),
                s.getEmail(),
                s.getStateCode(),
                s.getCityCode(),
                s.getJoiningDate(),
                s.getStatus(),
                s.getCreatedAt(),
                s.getUpdatedAt());
    }

    private static String emptyToNull(String x) {
        return x == null || x.isBlank() ? null : x.trim();
    }

    private static String trimNull(String x) {
        if (x == null) {
            return null;
        }
        String t = x.trim();
        return t.isEmpty() ? null : t;
    }

    private static String tempUniqueCode(String prefix) {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        int max = 32 - prefix.length();
        if (suffix.length() > max) {
            suffix = suffix.substring(0, max);
        }
        return prefix + suffix;
    }
}
