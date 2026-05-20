package com.shopmanagement.fieldforceservice.service.lead;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.LeadResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.LeadStatusPatch;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.LeadUpsert;
import com.shopmanagement.fieldforceservice.exception.ConflictException;
import com.shopmanagement.fieldforceservice.exception.NotFoundException;
import com.shopmanagement.fieldforceservice.model.BusinessLead;
import com.shopmanagement.fieldforceservice.model.LeadSource;
import com.shopmanagement.fieldforceservice.model.LeadStatus;
import com.shopmanagement.fieldforceservice.model.Promoter;
import com.shopmanagement.fieldforceservice.model.Salesman;
import com.shopmanagement.fieldforceservice.repository.BusinessLeadRepository;
import com.shopmanagement.fieldforceservice.repository.PromoterRepository;
import com.shopmanagement.fieldforceservice.repository.SalesmanRepository;
import com.shopmanagement.fieldforceservice.service.CommissionAccrualService;
import com.shopmanagement.fieldforceservice.support.MobileNormalizer;
import com.shopmanagement.fieldforceservice.support.TenantIds;

@Service
public class BusinessLeadService {

    private final BusinessLeadRepository leadRepository;
    private final PromoterRepository promoterRepository;
    private final SalesmanRepository salesmanRepository;
    private final DuplicateLeadDetectionService duplicateDetection;
    private final CommissionAccrualService commissionAccrualService;

    public BusinessLeadService(
            BusinessLeadRepository leadRepository,
            PromoterRepository promoterRepository,
            SalesmanRepository salesmanRepository,
            DuplicateLeadDetectionService duplicateDetection,
            CommissionAccrualService commissionAccrualService) {
        this.leadRepository = leadRepository;
        this.promoterRepository = promoterRepository;
        this.salesmanRepository = salesmanRepository;
        this.duplicateDetection = duplicateDetection;
        this.commissionAccrualService = commissionAccrualService;
    }

    @Transactional
    public LeadResponse create(LeadUpsert r, boolean skipDuplicateCheck) {
        if (!skipDuplicateCheck) {
            var dup = duplicateDetection.check(new com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.DuplicateCheckRequest(
                    r.mobile(), r.gstin(), r.businessName(), r.gpsLatitude(), r.gpsLongitude()));
            if (dup.hasDuplicates()) {
                throw new ConflictException("Potential duplicate lead detected; review /leads/duplicate-check");
            }
        }
        long tenantId = TenantIds.require();
        BusinessLead lead = new BusinessLead();
        lead.setTenantId(tenantId);
        lead.setLeadCode(tempUniqueCode("TMP-LD-"));
        apply(lead, r);
        lead = leadRepository.save(lead);
        lead.setLeadCode("LD-" + lead.getId());
        lead.touch();
        lead = leadRepository.save(lead);
        commissionAccrualService.accrueForLeadCreated(lead);
        return toResponse(lead);
    }

    @Transactional
    public LeadResponse update(Long id, LeadUpsert r) {
        BusinessLead lead = load(TenantIds.require(), id);
        apply(lead, r);
        lead.touch();
        return toResponse(leadRepository.save(lead));
    }

    @Transactional
    public LeadResponse patchStatus(Long id, LeadStatusPatch patch) {
        BusinessLead lead = load(TenantIds.require(), id);
        if (lead.getLeadStatus() == LeadStatus.CONVERTED) {
            throw new ConflictException("Converted leads cannot change status");
        }
        lead.setLeadStatus(patch.leadStatus());
        lead.touch();
        return toResponse(leadRepository.save(lead));
    }

    @Transactional
    public void softDelete(Long id) {
        BusinessLead lead = load(TenantIds.require(), id);
        lead.softDelete();
        leadRepository.save(lead);
    }

    @Transactional(readOnly = true)
    public LeadResponse get(Long id) {
        return toResponse(load(TenantIds.require(), id));
    }

    @Transactional(readOnly = true)
    public Page<LeadResponse> search(
            LeadStatus status, Long promoterId, Long salesmanId, String state, String city, String q, Pageable pageable) {
        long tenantId = TenantIds.require();
        return leadRepository
                .search(tenantId, status, promoterId, salesmanId, blank(state), blank(city), blank(q), pageable)
                .map(this::toResponse);
    }

    private BusinessLead load(long tenantId, Long id) {
        return leadRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Lead not found"));
    }

    private void apply(BusinessLead lead, LeadUpsert r) {
        long tenantId = lead.getTenantId() != null ? lead.getTenantId() : TenantIds.require();
        lead.setBusinessName(r.businessName().trim());
        lead.setOwnerName(trim(r.ownerName()));
        lead.setMobile(r.mobile().trim());
        lead.setMobileNormalized(MobileNormalizer.normalize(r.mobile()));
        lead.setAlternateMobile(trim(r.alternateMobile()));
        lead.setBusinessType(trim(r.businessType()));
        lead.setGstin(trim(r.gstin()));
        lead.setAddress(trim(r.address()));
        lead.setCity(trim(r.city()));
        lead.setStateCode(trim(r.stateCode()));
        lead.setPincode(trim(r.pincode()));
        lead.setGpsLatitude(r.gpsLatitude());
        lead.setGpsLongitude(r.gpsLongitude());
        if (r.leadSource() != null) {
            lead.setLeadSource(r.leadSource());
        } else if (lead.getLeadSource() == null) {
            lead.setLeadSource(LeadSource.FIELD_VISIT);
        }
        if (r.priority() != null) {
            lead.setPriority(r.priority());
        }
        lead.setRemarks(trim(r.remarks()));
        lead.setExpectedConversionDate(r.expectedConversionDate());

        Promoter promoter = resolvePromoter(tenantId, r.createdByPromoterId());
        Salesman salesman = resolveSalesman(tenantId, r.assignedSalesmanId());
        if (promoter == null && salesman != null) {
            promoter = salesman.getPromoter();
        }
        if (promoter != null) {
            lead.setCreatedByPromoter(promoter);
        }
        if (salesman != null) {
            lead.setAssignedSalesman(salesman);
        }
    }

    private Promoter resolvePromoter(long tenantId, Long id) {
        if (id == null) {
            return null;
        }
        return promoterRepository.findByTenantIdAndId(tenantId, id).orElseThrow(() -> new NotFoundException("Promoter not found"));
    }

    private Salesman resolveSalesman(long tenantId, Long id) {
        if (id == null) {
            return null;
        }
        return salesmanRepository.findByTenantIdAndId(tenantId, id).orElseThrow(() -> new NotFoundException("Salesman not found"));
    }

    private LeadResponse toResponse(BusinessLead l) {
        return new LeadResponse(
                l.getId(),
                l.getLeadCode(),
                l.getBusinessName(),
                l.getOwnerName(),
                l.getMobile(),
                l.getAlternateMobile(),
                l.getBusinessType(),
                l.getGstin(),
                l.getAddress(),
                l.getCity(),
                l.getStateCode(),
                l.getPincode(),
                l.getGpsLatitude(),
                l.getGpsLongitude(),
                l.getLeadSource(),
                l.getCreatedByPromoter() == null ? null : l.getCreatedByPromoter().getId(),
                l.getAssignedSalesman() == null ? null : l.getAssignedSalesman().getId(),
                l.getLeadStatus(),
                l.getPriority(),
                l.getRemarks(),
                l.getExpectedConversionDate(),
                l.getConvertedAt(),
                l.getExternalMerchantId(),
                l.getExternalShopId(),
                l.getCreatedAt(),
                l.getUpdatedAt());
    }

    private static String tempUniqueCode(String prefix) {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        int max = 32 - prefix.length();
        if (suffix.length() > max) {
            suffix = suffix.substring(0, max);
        }
        return prefix + suffix;
    }

    private static String trim(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String blank(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
