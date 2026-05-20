package com.shopmanagement.fieldforceservice.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.fieldforceservice.api.FieldforceApi.DashboardSummary;
import com.shopmanagement.fieldforceservice.model.ApprovalStatus;
import com.shopmanagement.fieldforceservice.model.CommissionEntryStatus;
import com.shopmanagement.fieldforceservice.repository.CommissionEntryRepository;
import com.shopmanagement.fieldforceservice.repository.PromoterRepository;
import com.shopmanagement.fieldforceservice.repository.SalesmanRepository;
import com.shopmanagement.fieldforceservice.repository.ShopRegistrationRepository;
import com.shopmanagement.fieldforceservice.support.TenantIds;

@Service
public class DashboardService {

    private final PromoterRepository promoterRepository;
    private final SalesmanRepository salesmanRepository;
    private final ShopRegistrationRepository shopRegistrationRepository;
    private final CommissionEntryRepository commissionEntryRepository;

    public DashboardService(
            PromoterRepository promoterRepository,
            SalesmanRepository salesmanRepository,
            ShopRegistrationRepository shopRegistrationRepository,
            CommissionEntryRepository commissionEntryRepository) {
        this.promoterRepository = promoterRepository;
        this.salesmanRepository = salesmanRepository;
        this.shopRegistrationRepository = shopRegistrationRepository;
        this.commissionEntryRepository = commissionEntryRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummary summary() {
        long tenantId = TenantIds.require();
        long promoters = promoterRepository.countByTenantId(tenantId);
        long salesmen = salesmanRepository.countByTenantId(tenantId);
        long shops = shopRegistrationRepository.countByTenantId(tenantId);
        long shopsPendingApproval =
                shopRegistrationRepository.countByTenantIdAndApprovalStatus(tenantId, ApprovalStatus.PENDING);
        long pendingLines = commissionEntryRepository.countByTenantIdAndStatus(tenantId, CommissionEntryStatus.PENDING);
        BigDecimal pendingAmt =
                commissionEntryRepository.sumAmountByTenantAndStatus(tenantId, CommissionEntryStatus.PENDING);
        if (pendingAmt == null) {
            pendingAmt = BigDecimal.ZERO;
        }
        return new DashboardSummary(promoters, salesmen, shops, shopsPendingApproval, pendingLines, pendingAmt);
    }
}
