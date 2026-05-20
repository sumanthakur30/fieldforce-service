package com.shopmanagement.fieldforceservice.service.analytics;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ExtendedDashboardSummary;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.LeadFunnelAnalytics;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.PerformanceAnalytics;
import com.shopmanagement.fieldforceservice.model.ApprovalStatus;
import com.shopmanagement.fieldforceservice.model.CommissionEntryStatus;
import com.shopmanagement.fieldforceservice.model.LeadStatus;
import com.shopmanagement.fieldforceservice.repository.BusinessLeadRepository;
import com.shopmanagement.fieldforceservice.repository.CommissionEntryRepository;
import com.shopmanagement.fieldforceservice.repository.FieldActivityRepository;
import com.shopmanagement.fieldforceservice.repository.PromoterRepository;
import com.shopmanagement.fieldforceservice.repository.SalesmanRepository;
import com.shopmanagement.fieldforceservice.repository.ShopRegistrationRepository;
import com.shopmanagement.fieldforceservice.support.TenantIds;

@Service
public class FieldforceAnalyticsService {

    private final BusinessLeadRepository leadRepository;
    private final PromoterRepository promoterRepository;
    private final SalesmanRepository salesmanRepository;
    private final ShopRegistrationRepository shopRegistrationRepository;
    private final CommissionEntryRepository commissionEntryRepository;
    private final FieldActivityRepository activityRepository;

    public FieldforceAnalyticsService(
            BusinessLeadRepository leadRepository,
            PromoterRepository promoterRepository,
            SalesmanRepository salesmanRepository,
            ShopRegistrationRepository shopRegistrationRepository,
            CommissionEntryRepository commissionEntryRepository,
            FieldActivityRepository activityRepository) {
        this.leadRepository = leadRepository;
        this.promoterRepository = promoterRepository;
        this.salesmanRepository = salesmanRepository;
        this.shopRegistrationRepository = shopRegistrationRepository;
        this.commissionEntryRepository = commissionEntryRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public LeadFunnelAnalytics funnel() {
        long tenantId = TenantIds.require();
        Map<LeadStatus, Long> counts = new EnumMap<>(LeadStatus.class);
        for (LeadStatus s : LeadStatus.values()) {
            counts.put(s, 0L);
        }
        List<Object[]> rows = leadRepository.countByStatus(tenantId);
        for (Object[] row : rows) {
            counts.put((LeadStatus) row[0], (Long) row[1]);
        }
        long total = leadRepository.countByTenantIdAndDeletedAtIsNull(tenantId);
        long converted = leadRepository.countByTenantIdAndLeadStatusAndDeletedAtIsNull(tenantId, LeadStatus.CONVERTED);
        return new LeadFunnelAnalytics(counts, total, converted);
    }

    @Transactional(readOnly = true)
    public PerformanceAnalytics performance() {
        long tenantId = TenantIds.require();
        long total = leadRepository.countByTenantIdAndDeletedAtIsNull(tenantId);
        long converted = leadRepository.countByTenantIdAndLeadStatusAndDeletedAtIsNull(tenantId, LeadStatus.CONVERTED);
        double rate = total == 0 ? 0.0 : (converted * 100.0) / total;
        long activities = activityRepository.findByTenantIdAndDeletedAtIsNullOrderByActivityAtDesc(tenantId, org.springframework.data.domain.Pageable.ofSize(1)).getTotalElements();
        return new PerformanceAnalytics(total, converted, rate, activities, 0);
    }

    @Transactional(readOnly = true)
    public ExtendedDashboardSummary extendedSummary() {
        long tenantId = TenantIds.require();
        java.math.BigDecimal pendingAmt =
                commissionEntryRepository.sumAmountByTenantAndStatus(tenantId, CommissionEntryStatus.PENDING);
        if (pendingAmt == null) {
            pendingAmt = java.math.BigDecimal.ZERO;
        }
        return new ExtendedDashboardSummary(
                promoterRepository.countByTenantId(tenantId),
                salesmanRepository.countByTenantId(tenantId),
                leadRepository.countByTenantIdAndDeletedAtIsNull(tenantId),
                leadRepository.countByTenantIdAndLeadStatusAndDeletedAtIsNull(tenantId, LeadStatus.CONVERTED),
                activityRepository.findByTenantIdAndDeletedAtIsNullOrderByActivityAtDesc(tenantId, org.springframework.data.domain.Pageable.ofSize(1)).getTotalElements(),
                shopRegistrationRepository.countByTenantId(tenantId),
                shopRegistrationRepository.countByTenantIdAndApprovalStatus(tenantId, ApprovalStatus.PENDING),
                commissionEntryRepository.countByTenantIdAndStatus(tenantId, CommissionEntryStatus.PENDING),
                pendingAmt);
    }
}
