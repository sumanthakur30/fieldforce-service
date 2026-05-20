package com.shopmanagement.fieldforceservice.service.conversion;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ConversionCompleteRequest;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ConversionResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ConversionUpsert;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ShopCreationResult;
import com.shopmanagement.fieldforceservice.exception.ConflictException;
import com.shopmanagement.fieldforceservice.exception.NotFoundException;
import com.shopmanagement.fieldforceservice.integration.ShopConversionClient;
import com.shopmanagement.fieldforceservice.model.BusinessLead;
import com.shopmanagement.fieldforceservice.model.ConversionStage;
import com.shopmanagement.fieldforceservice.model.LeadConversion;
import com.shopmanagement.fieldforceservice.model.LeadStatus;
import com.shopmanagement.fieldforceservice.repository.BusinessLeadRepository;
import com.shopmanagement.fieldforceservice.repository.LeadConversionRepository;
import com.shopmanagement.fieldforceservice.service.CommissionAccrualService;
import com.shopmanagement.fieldforceservice.support.TenantIds;

@Service
public class LeadConversionService {

    private final LeadConversionRepository conversionRepository;
    private final BusinessLeadRepository leadRepository;
    private final ShopConversionClient shopConversionClient;
    private final CommissionAccrualService commissionAccrualService;

    public LeadConversionService(
            LeadConversionRepository conversionRepository,
            BusinessLeadRepository leadRepository,
            ShopConversionClient shopConversionClient,
            CommissionAccrualService commissionAccrualService) {
        this.conversionRepository = conversionRepository;
        this.leadRepository = leadRepository;
        this.shopConversionClient = shopConversionClient;
        this.commissionAccrualService = commissionAccrualService;
    }

    @Transactional
    public ConversionResponse startOrUpdate(Long leadId, ConversionUpsert body) {
        long tenantId = TenantIds.require();
        BusinessLead lead = loadLead(tenantId, leadId);
        if (lead.getLeadStatus() == LeadStatus.CONVERTED) {
            throw new ConflictException("Lead already converted");
        }
        LeadConversion conversion = conversionRepository
                .findByTenantIdAndBusinessLeadId(tenantId, leadId)
                .orElseGet(() -> newConversion(tenantId, lead));

        conversion.setCurrentStage(body.stage());
        if (body.subscriptionPlanCode() != null) {
            conversion.setSubscriptionPlanCode(body.subscriptionPlanCode());
        }
        if (body.kycVerified() != null) {
            conversion.setKycVerified(body.kycVerified());
        }
        if (body.merchantPayloadJson() != null) {
            conversion.setMerchantPayloadJson(body.merchantPayloadJson());
        }
        conversion.setUpdatedAt(Instant.now());
        conversion = conversionRepository.save(conversion);

        if (body.stage() == ConversionStage.SUBSCRIPTION_SELECTION) {
            lead.setLeadStatus(LeadStatus.NEGOTIATION);
        }
        lead.touch();
        leadRepository.save(lead);
        return toResponse(conversion, null);
    }

    @Transactional
    public ConversionResponse complete(Long leadId, ConversionCompleteRequest body) {
        long tenantId = TenantIds.require();
        BusinessLead lead = loadLead(tenantId, leadId);
        if (lead.getLeadStatus() == LeadStatus.CONVERTED) {
            throw new ConflictException("Lead already converted");
        }

        LeadConversion conversion = conversionRepository
                .findByTenantIdAndBusinessLeadId(tenantId, leadId)
                .orElseGet(() -> newConversion(tenantId, lead));

        String planCode = body != null && body.subscriptionPlanCode() != null
                ? body.subscriptionPlanCode()
                : conversion.getSubscriptionPlanCode();
        String payload = body != null ? body.merchantPayloadJson() : conversion.getMerchantPayloadJson();
        String ownerEmail = body != null ? body.ownerEmail() : null;
        String ownerUsername = body != null ? body.ownerUsername() : null;

        conversion.setCurrentStage(ConversionStage.MERCHANT_CREATION);
        conversion.setUpdatedAt(Instant.now());
        conversionRepository.save(conversion);

        try {
            ShopCreationResult created = shopConversionClient.createMerchantAndShop(
                    tenantId, lead, planCode, payload, ownerEmail, ownerUsername);
            conversion.setExternalMerchantId(created.externalMerchantId());
            conversion.setExternalShopId(created.externalShopId());
            conversion.setCurrentStage(ConversionStage.COMPLETED);
            conversion.setCompletedAt(Instant.now());
            conversion.setUpdatedAt(Instant.now());
            conversionRepository.save(conversion);

            lead.setLeadStatus(LeadStatus.CONVERTED);
            lead.setConvertedAt(Instant.now());
            lead.setExternalMerchantId(created.externalMerchantId());
            lead.setExternalShopId(created.externalShopId());
            lead.touch();
            leadRepository.save(lead);

            commissionAccrualService.accrueForLeadConversion(lead);
            return toResponse(conversion, created);
        } catch (RuntimeException ex) {
            conversion.setCurrentStage(ConversionStage.FAILED);
            conversion.setFailureReason(ex.getMessage());
            conversion.setUpdatedAt(Instant.now());
            conversionRepository.save(conversion);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public ConversionResponse get(Long leadId) {
        long tenantId = TenantIds.require();
        LeadConversion conversion = conversionRepository
                .findByTenantIdAndBusinessLeadId(tenantId, leadId)
                .orElseThrow(() -> new NotFoundException("Conversion not started for lead"));
        return toResponse(conversion, null);
    }

    private LeadConversion newConversion(long tenantId, BusinessLead lead) {
        LeadConversion c = new LeadConversion();
        c.setTenantId(tenantId);
        c.setBusinessLead(lead);
        c.setCurrentStage(ConversionStage.VERIFICATION);
        return c;
    }

    private BusinessLead loadLead(long tenantId, Long leadId) {
        return leadRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantId, leadId)
                .orElseThrow(() -> new NotFoundException("Lead not found"));
    }

    private ConversionResponse toResponse(LeadConversion c, ShopCreationResult shopResult) {
        return new ConversionResponse(
                c.getId(),
                c.getBusinessLead().getId(),
                c.getCurrentStage(),
                c.getSubscriptionPlanCode(),
                c.isKycVerified(),
                c.getExternalMerchantId(),
                c.getExternalShopId(),
                c.getFailureReason(),
                c.getStartedAt(),
                c.getCompletedAt(),
                shopResult != null ? shopResult.ownerInviteEmailQueued() : null,
                shopResult != null ? shopResult.inviteExpiresAt() : null);
    }
}
