package com.shopmanagement.fieldforceservice.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.fieldforceservice.api.FieldforceApi.PromoterDetailResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.PromoterResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.PromoterUpsert;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.TerritoryResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceApi.TerritoryUpsert;
import com.shopmanagement.fieldforceservice.exception.ConflictException;
import com.shopmanagement.fieldforceservice.exception.NotFoundException;
import com.shopmanagement.fieldforceservice.model.CommissionBeneficiaryType;
import com.shopmanagement.fieldforceservice.model.CommissionEntryStatus;
import com.shopmanagement.fieldforceservice.model.FieldPersonStatus;
import com.shopmanagement.fieldforceservice.model.Promoter;
import com.shopmanagement.fieldforceservice.model.PromoterTerritory;
import com.shopmanagement.fieldforceservice.repository.BusinessLeadRepository;
import com.shopmanagement.fieldforceservice.repository.CommissionEntryRepository;
import com.shopmanagement.fieldforceservice.repository.PromoterRepository;
import com.shopmanagement.fieldforceservice.repository.PromoterTerritoryRepository;
import com.shopmanagement.fieldforceservice.repository.SalesmanRepository;
import com.shopmanagement.fieldforceservice.repository.ShopRegistrationRepository;
import com.shopmanagement.fieldforceservice.support.TenantIds;

@Service
public class PromoterService {

    private final PromoterRepository promoterRepository;
    private final PromoterTerritoryRepository territoryRepository;
    private final SalesmanRepository salesmanRepository;
    private final ShopRegistrationRepository shopRegistrationRepository;
    private final CommissionEntryRepository commissionEntryRepository;
    private final BusinessLeadRepository businessLeadRepository;

    public PromoterService(
            PromoterRepository promoterRepository,
            PromoterTerritoryRepository territoryRepository,
            SalesmanRepository salesmanRepository,
            ShopRegistrationRepository shopRegistrationRepository,
            CommissionEntryRepository commissionEntryRepository,
            BusinessLeadRepository businessLeadRepository) {
        this.promoterRepository = promoterRepository;
        this.territoryRepository = territoryRepository;
        this.salesmanRepository = salesmanRepository;
        this.shopRegistrationRepository = shopRegistrationRepository;
        this.commissionEntryRepository = commissionEntryRepository;
        this.businessLeadRepository = businessLeadRepository;
    }

    @Transactional
    public PromoterResponse create(PromoterUpsert r) {
        long tenantId = TenantIds.require();
        Promoter p = new Promoter();
        p.setTenantId(tenantId);
        p.setPromoterCode(tempUniqueCode("TMP-P-"));
        applyUpsert(p, r);
        p = promoterRepository.save(p);
        p.setPromoterCode("PF-" + p.getId());
        p.setUpdatedAt(Instant.now());
        promoterRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public PromoterResponse update(Long id, PromoterUpsert r) {
        Promoter p = load(TenantIds.require(), id);
        applyUpsert(p, r);
        p.setUpdatedAt(Instant.now());
        promoterRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public PromoterResponse setStatus(Long id, FieldPersonStatus status) {
        Promoter p = load(TenantIds.require(), id);
        p.setStatus(status);
        p.setUpdatedAt(Instant.now());
        promoterRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public void delete(Long id) {
        long tenantId = TenantIds.require();
        Promoter p = load(tenantId, id);
        if (salesmanRepository.countByTenantIdAndPromoter(tenantId, p) > 0) {
            throw new ConflictException(
                    "Cannot delete promoter with salesmen. Deactivate the promoter or remove salesmen first.");
        }
        if (shopRegistrationRepository.countByTenantIdAndPromoter(tenantId, p) > 0) {
            throw new ConflictException(
                    "Cannot delete promoter linked to shop registrations. Deactivate instead.");
        }
        if (businessLeadRepository.countByTenantIdAndCreatedByPromoterIdAndDeletedAtIsNull(tenantId, id) > 0) {
            throw new ConflictException("Cannot delete promoter with business leads. Deactivate instead.");
        }
        territoryRepository.deleteByPromoter(p);
        promoterRepository.delete(p);
    }

    @Transactional(readOnly = true)
    public PromoterDetailResponse get(Long id) {
        long tenantId = TenantIds.require();
        Promoter p = load(tenantId, id);
        PromoterResponse pr = toResponse(p);
        long terr = territoryRepository.countByPromoter(p);
        long sales = salesmanRepository.countByTenantIdAndPromoter(tenantId, p);
        long shops = shopRegistrationRepository.countByTenantIdAndPromoter(tenantId, p);
        BigDecimal pending = commissionEntryRepository.sumAmount(
                tenantId, CommissionBeneficiaryType.PROMOTER, id, CommissionEntryStatus.PENDING);
        if (pending == null) {
            pending = BigDecimal.ZERO;
        }
        return new PromoterDetailResponse(pr, terr, sales, shops, pending);
    }

    @Transactional(readOnly = true)
    public Page<PromoterResponse> search(String state, String city, FieldPersonStatus status, String q, Pageable pageable) {
        long tenantId = TenantIds.require();
        return promoterRepository.search(tenantId, emptyToNull(state), emptyToNull(city), status, emptyToNull(q), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TerritoryResponse> listTerritories(Long promoterId) {
        Promoter p = load(TenantIds.require(), promoterId);
        return territoryRepository.findByPromoter(p).stream()
                .map(t -> new TerritoryResponse(t.getId(), t.getStateCode(), t.getCityCode(), t.getCreatedAt()))
                .toList();
    }

    @Transactional
    public TerritoryResponse addTerritory(Long promoterId, TerritoryUpsert u) {
        long tenantId = TenantIds.require();
        Promoter p = load(tenantId, promoterId);
        PromoterTerritory t = new PromoterTerritory();
        t.setTenantId(tenantId);
        t.setPromoter(p);
        t.setStateCode(u.stateCode().trim());
        t.setCityCode(u.cityCode().trim());
        try {
            territoryRepository.save(t);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("That city is already assigned to a promoter in this tenant.");
        }
        return new TerritoryResponse(t.getId(), t.getStateCode(), t.getCityCode(), t.getCreatedAt());
    }

    @Transactional
    public void deleteTerritory(Long promoterId, Long territoryId) {
        Promoter p = load(TenantIds.require(), promoterId);
        PromoterTerritory t = territoryRepository.findById(territoryId).orElseThrow(() -> new NotFoundException("Territory not found"));
        if (!t.getPromoter().getId().equals(p.getId())) {
            throw new ConflictException("Territory does not belong to this promoter");
        }
        territoryRepository.delete(t);
    }

    private Promoter load(long tenantId, Long id) {
        Promoter p = promoterRepository.findByTenantIdAndId(tenantId, id).orElseThrow(() -> new NotFoundException("Promoter not found"));
        return p;
    }

    private static void applyUpsert(Promoter p, PromoterUpsert r) {
        p.setFullName(r.fullName().trim());
        p.setMobile(r.mobile().trim());
        p.setEmail(trimNull(r.email()));
        p.setAddress(trimNull(r.address()));
        p.setStateCode(r.stateCode().trim());
        p.setCityCode(r.cityCode().trim());
        p.setProfilePhotoUrl(trimNull(r.profilePhotoUrl()));
        p.setJoiningDate(r.joiningDate());
        p.setPan(trimNull(r.pan()));
        p.setAadhaarMasked(trimNull(r.aadhaarMasked()));
        p.setGstin(trimNull(r.gstin()));
        p.setBankAccountName(trimNull(r.bankAccountName()));
        p.setBankIfsc(trimNull(r.bankIfsc()));
        p.setBankAccountNumber(trimNull(r.bankAccountNumber()));
    }

    private PromoterResponse toResponse(Promoter p) {
        return new PromoterResponse(
                p.getId(),
                p.getPromoterCode(),
                p.getFullName(),
                p.getMobile(),
                p.getEmail(),
                p.getAddress(),
                p.getStateCode(),
                p.getCityCode(),
                p.getProfilePhotoUrl(),
                p.getJoiningDate(),
                p.getStatus(),
                p.getPan(),
                p.getAadhaarMasked(),
                p.getGstin(),
                p.getBankAccountName(),
                p.getBankIfsc(),
                p.getBankAccountNumber(),
                p.getCreatedAt(),
                p.getUpdatedAt());
    }

    private static String emptyToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    private static String trimNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** Placeholder until PK is assigned (promoter_code is NOT NULL in DB). */
    private static String tempUniqueCode(String prefix) {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        int max = 32 - prefix.length();
        if (suffix.length() > max) {
            suffix = suffix.substring(0, max);
        }
        return prefix + suffix;
    }
}
