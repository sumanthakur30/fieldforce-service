package com.shopmanagement.fieldforceservice.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.fieldforceservice.api.FieldforceApi.CommissionPlanResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.CommissionPlanUpsert;
import com.shopmanagement.fieldforceservice.exception.NotFoundException;
import com.shopmanagement.fieldforceservice.model.CommissionPlan;
import com.shopmanagement.fieldforceservice.repository.CommissionPlanRepository;
import com.shopmanagement.fieldforceservice.support.TenantIds;

@Service
public class CommissionPlanService {

    private final CommissionPlanRepository commissionPlanRepository;

    public CommissionPlanService(CommissionPlanRepository commissionPlanRepository) {
        this.commissionPlanRepository = commissionPlanRepository;
    }

    @Transactional(readOnly = true)
    public List<CommissionPlanResponse> list() {
        long tenantId = TenantIds.require();
        return commissionPlanRepository.findByTenantIdAndActiveTrueOrderByEffectiveFromDesc(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CommissionPlanResponse create(CommissionPlanUpsert u) {
        CommissionPlan p = new CommissionPlan();
        p.setTenantId(TenantIds.require());
        apply(p, u);
        p.setCreatedAt(Instant.now());
        commissionPlanRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public CommissionPlanResponse update(Long id, CommissionPlanUpsert u) {
        CommissionPlan p = load(id);
        apply(p, u);
        commissionPlanRepository.save(p);
        return toResponse(p);
    }

    private CommissionPlan load(Long id) {
        long tenantId = TenantIds.require();
        CommissionPlan p = commissionPlanRepository.findById(id).orElseThrow(() -> new NotFoundException("Commission plan not found"));
        if (!p.getTenantId().equals(tenantId)) {
            throw new NotFoundException("Commission plan not found");
        }
        return p;
    }

    private void apply(CommissionPlan p, CommissionPlanUpsert u) {
        p.setName(u.name().trim());
        p.setPromoterFixedAmount(u.promoterFixedAmount());
        p.setPromoterPercent(u.promoterPercent());
        p.setSalesmanFixedAmount(u.salesmanFixedAmount());
        p.setSalesmanPercent(u.salesmanPercent());
        p.setEffectiveFrom(u.effectiveFrom());
        p.setEffectiveTo(u.effectiveTo());
        p.setActive(u.active());
    }

    private CommissionPlanResponse toResponse(CommissionPlan p) {
        return new CommissionPlanResponse(
                p.getId(),
                p.getName(),
                p.getPromoterFixedAmount(),
                p.getPromoterPercent(),
                p.getSalesmanFixedAmount(),
                p.getSalesmanPercent(),
                p.getEffectiveFrom(),
                p.getEffectiveTo(),
                p.isActive(),
                p.getCreatedAt());
    }
}
