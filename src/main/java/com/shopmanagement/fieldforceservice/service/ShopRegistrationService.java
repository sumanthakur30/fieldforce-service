package com.shopmanagement.fieldforceservice.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.fieldforceservice.api.FieldforceApi.ApprovalPatch;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.ShopRegistrationResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.ShopRegistrationUpsert;
import com.shopmanagement.fieldforceservice.exception.NotFoundException;
import com.shopmanagement.fieldforceservice.model.ApprovalStatus;
import com.shopmanagement.fieldforceservice.model.Promoter;
import com.shopmanagement.fieldforceservice.model.Salesman;
import com.shopmanagement.fieldforceservice.model.ShopRegistration;
import com.shopmanagement.fieldforceservice.repository.PromoterRepository;
import com.shopmanagement.fieldforceservice.repository.SalesmanRepository;
import com.shopmanagement.fieldforceservice.repository.ShopRegistrationRepository;
import com.shopmanagement.fieldforceservice.support.TenantIds;

@Service
public class ShopRegistrationService {

    private final ShopRegistrationRepository shopRegistrationRepository;
    private final PromoterRepository promoterRepository;
    private final SalesmanRepository salesmanRepository;
    private final CommissionAccrualService commissionAccrualService;

    public ShopRegistrationService(
            ShopRegistrationRepository shopRegistrationRepository,
            PromoterRepository promoterRepository,
            SalesmanRepository salesmanRepository,
            CommissionAccrualService commissionAccrualService) {
        this.shopRegistrationRepository = shopRegistrationRepository;
        this.promoterRepository = promoterRepository;
        this.salesmanRepository = salesmanRepository;
        this.commissionAccrualService = commissionAccrualService;
    }

    @Transactional
    public ShopRegistrationResponse upsert(ShopRegistrationUpsert r) {
        long tenantId = TenantIds.require();
        ShopRegistration reg = shopRegistrationRepository
                .findByTenantIdAndExternalShopId(tenantId, r.externalShopId().trim())
                .orElseGet(ShopRegistration::new);
        reg.setTenantId(tenantId);
        reg.setExternalShopId(r.externalShopId().trim());
        reg.setShopName(r.shopName().trim());
        reg.setOwnerName(trimNull(r.ownerName()));
        reg.setOwnerMobile(trimNull(r.ownerMobile()));
        reg.setAddress(trimNull(r.address()));
        reg.setStateCode(trimNull(r.stateCode()));
        reg.setCityCode(trimNull(r.cityCode()));
        Salesman salesman = resolveSalesman(tenantId, r.salesmanId());
        Promoter promoter = resolvePromoter(tenantId, r.promoterId());
        if (promoter == null && salesman != null) {
            promoter = salesman.getPromoter();
        }
        reg.setPromoter(promoter);
        reg.setSalesman(salesman);
        if (r.registeredAt() != null) {
            reg.setRegisteredAt(r.registeredAt());
        }
        reg.setUpdatedAt(Instant.now());
        reg = shopRegistrationRepository.save(reg);
        return toResponse(reg);
    }

    @Transactional
    public ShopRegistrationResponse patchApproval(Long id, ApprovalPatch patch) {
        long tenantId = TenantIds.require();
        ShopRegistration reg = shopRegistrationRepository.findById(id).orElseThrow(() -> new NotFoundException("Shop registration not found"));
        if (!reg.getTenantId().equals(tenantId)) {
            throw new NotFoundException("Shop registration not found");
        }
        reg.setApprovalStatus(patch.approvalStatus());
        reg.setUpdatedAt(Instant.now());
        shopRegistrationRepository.save(reg);
        if (patch.approvalStatus() == ApprovalStatus.APPROVED) {
            commissionAccrualService.accrueForApprovedShop(reg);
        }
        return toResponse(reg);
    }

    @Transactional(readOnly = true)
    public ShopRegistrationResponse get(Long id) {
        return toResponse(load(TenantIds.require(), id));
    }

    @Transactional(readOnly = true)
    public Page<ShopRegistrationResponse> filter(
            String state, String city, ApprovalStatus status, Long promoterId, Long salesmanId, Pageable pageable) {
        long tenantId = TenantIds.require();
        return shopRegistrationRepository
                .filter(
                        tenantId,
                        emptyToNull(state),
                        emptyToNull(city),
                        status,
                        promoterId,
                        salesmanId,
                        pageable)
                .map(this::toResponse);
    }

    private ShopRegistration load(long tenantId, Long id) {
        ShopRegistration reg = shopRegistrationRepository.findById(id).orElseThrow(() -> new NotFoundException("Shop registration not found"));
        if (!reg.getTenantId().equals(tenantId)) {
            throw new NotFoundException("Shop registration not found");
        }
        return reg;
    }

    private Promoter resolvePromoter(long tenantId, Long promoterId) {
        if (promoterId == null) {
            return null;
        }
        return promoterRepository.findByTenantIdAndId(tenantId, promoterId).orElseThrow(() -> new NotFoundException("Promoter not found"));
    }

    private Salesman resolveSalesman(long tenantId, Long salesmanId) {
        if (salesmanId == null) {
            return null;
        }
        return salesmanRepository.findByTenantIdAndId(tenantId, salesmanId).orElseThrow(() -> new NotFoundException("Salesman not found"));
    }

    private ShopRegistrationResponse toResponse(ShopRegistration r) {
        return new ShopRegistrationResponse(
                r.getId(),
                r.getExternalShopId(),
                r.getShopName(),
                r.getOwnerName(),
                r.getOwnerMobile(),
                r.getAddress(),
                r.getStateCode(),
                r.getCityCode(),
                r.getRegisteredAt(),
                r.getPromoter() == null ? null : r.getPromoter().getId(),
                r.getSalesman() == null ? null : r.getSalesman().getId(),
                r.getApprovalStatus(),
                r.getUpdatedAt());
    }

    private static String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static String trimNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
