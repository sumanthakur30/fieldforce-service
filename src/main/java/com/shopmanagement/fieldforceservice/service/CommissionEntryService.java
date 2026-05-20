package com.shopmanagement.fieldforceservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.shopmanagement.fieldforceservice.api.FieldforceApi.CommissionEntryResponse;
import com.shopmanagement.fieldforceservice.model.CommissionBeneficiaryType;
import com.shopmanagement.fieldforceservice.model.CommissionEntry;
import com.shopmanagement.fieldforceservice.model.CommissionEntryStatus;
import com.shopmanagement.fieldforceservice.repository.CommissionEntryRepository;
import com.shopmanagement.fieldforceservice.repository.CommissionEntrySpecifications;
import com.shopmanagement.fieldforceservice.support.TenantIds;

@Service
public class CommissionEntryService {

    private final CommissionEntryRepository commissionEntryRepository;

    public CommissionEntryService(CommissionEntryRepository commissionEntryRepository) {
        this.commissionEntryRepository = commissionEntryRepository;
    }

    public Page<CommissionEntryResponse> search(
            CommissionEntryStatus status,
            Long shopRegistrationId,
            CommissionBeneficiaryType beneficiaryType,
            Long beneficiaryId,
            Pageable pageable) {
        long tenantId = TenantIds.require();
        return commissionEntryRepository
                .findAll(
                        CommissionEntrySpecifications.search(
                                tenantId, status, shopRegistrationId, beneficiaryType, beneficiaryId),
                        pageable)
                .map(this::toResponse);
    }

    private CommissionEntryResponse toResponse(CommissionEntry e) {
        return new CommissionEntryResponse(
                e.getId(),
                e.getShopRegistrationId(),
                e.getBeneficiaryType(),
                e.getBeneficiaryId(),
                e.getAmount(),
                e.getStatus(),
                e.getPeriodMonth(),
                e.getCreatedAt(),
                e.getPaidAt(),
                e.getPayoutReference());
    }
}
