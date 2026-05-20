package com.shopmanagement.fieldforceservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.fieldforceservice.model.ApprovalStatus;
import com.shopmanagement.fieldforceservice.model.BusinessLead;
import com.shopmanagement.fieldforceservice.model.CommissionBeneficiaryType;
import com.shopmanagement.fieldforceservice.model.CommissionEntry;
import com.shopmanagement.fieldforceservice.model.CommissionEntryStatus;
import com.shopmanagement.fieldforceservice.model.CommissionEvent;
import com.shopmanagement.fieldforceservice.model.CommissionPlan;
import com.shopmanagement.fieldforceservice.model.ShopRegistration;
import com.shopmanagement.fieldforceservice.repository.CommissionEntryRepository;
import com.shopmanagement.fieldforceservice.repository.CommissionPlanRepository;

@Service
public class CommissionAccrualService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal PERCENT_BASE = new BigDecimal("100.00");

    private final CommissionPlanRepository commissionPlanRepository;
    private final CommissionEntryRepository commissionEntryRepository;

    public CommissionAccrualService(
            CommissionPlanRepository commissionPlanRepository,
            CommissionEntryRepository commissionEntryRepository) {
        this.commissionPlanRepository = commissionPlanRepository;
        this.commissionEntryRepository = commissionEntryRepository;
    }

    /**
     * Creates commission ledger lines when a shop is approved (idempotent per shop registration row).
     */
    @Transactional
    public void accrueForLeadCreated(BusinessLead lead) {
        accrueLeadEvent(lead, CommissionEvent.LEAD_CREATED, plan -> plan.getLeadCreatedAmount());
    }

    @Transactional
    public void accrueForDemoCompleted(BusinessLead lead) {
        accrueLeadEvent(lead, CommissionEvent.DEMO_COMPLETED, plan -> plan.getDemoCompletedAmount());
    }

    @Transactional
    public void accrueForLeadConversion(BusinessLead lead) {
        accrueLeadEvent(lead, CommissionEvent.CONVERSION, plan -> plan.getConversionAmount());
    }

    private void accrueLeadEvent(
            BusinessLead lead,
            CommissionEvent event,
            java.util.function.Function<CommissionPlan, BigDecimal> amountFn) {
        if (commissionEntryRepository.existsByTenantIdAndBusinessLeadIdAndCommissionEvent(
                lead.getTenantId(), lead.getId(), event)) {
            return;
        }
        LocalDate onDate = LocalDate.ofInstant(lead.getUpdatedAt(), ZoneId.systemDefault());
        List<CommissionPlan> plans = commissionPlanRepository.findApplicable(lead.getTenantId(), onDate);
        if (plans.isEmpty()) {
            return;
        }
        CommissionPlan plan = plans.get(0);
        BigDecimal perPerson = amountFn.apply(plan);
        if (perPerson == null || perPerson.compareTo(ZERO) <= 0) {
            return;
        }
        LocalDate periodMonth = onDate.withDayOfMonth(1);
        if (lead.getCreatedByPromoter() != null) {
            commissionEntryRepository.save(leadEntry(lead, CommissionBeneficiaryType.PROMOTER, lead.getCreatedByPromoter().getId(), perPerson, periodMonth, event));
        }
        if (lead.getAssignedSalesman() != null) {
            commissionEntryRepository.save(leadEntry(lead, CommissionBeneficiaryType.SALESMAN, lead.getAssignedSalesman().getId(), perPerson, periodMonth, event));
        }
    }

    @Transactional
    public void accrueForApprovedShop(ShopRegistration reg) {
        if (reg.getApprovalStatus() != ApprovalStatus.APPROVED) {
            return;
        }
        if (commissionEntryRepository.existsByShopRegistrationId(reg.getId())) {
            return;
        }
        LocalDate onDate = LocalDate.ofInstant(reg.getRegisteredAt(), ZoneId.systemDefault());
        List<CommissionPlan> plans = commissionPlanRepository.findApplicable(reg.getTenantId(), onDate);
        if (plans.isEmpty()) {
            return;
        }
        CommissionPlan plan = plans.get(0);
        LocalDate periodMonth = onDate.withDayOfMonth(1);

        if (reg.getPromoter() != null) {
            BigDecimal amt = resolvePromoterAmount(plan);
            if (amt != null && amt.compareTo(ZERO) > 0) {
                commissionEntryRepository.save(entry(reg, CommissionBeneficiaryType.PROMOTER, reg.getPromoter().getId(), amt, periodMonth));
            }
        }
        if (reg.getSalesman() != null) {
            BigDecimal amt = resolveSalesmanAmount(plan);
            if (amt != null && amt.compareTo(ZERO) > 0) {
                commissionEntryRepository.save(entry(reg, CommissionBeneficiaryType.SALESMAN, reg.getSalesman().getId(), amt, periodMonth));
            }
        }
    }

    private static BigDecimal resolvePromoterAmount(CommissionPlan plan) {
        if (plan.getPromoterFixedAmount() != null && plan.getPromoterFixedAmount().compareTo(ZERO) > 0) {
            return plan.getPromoterFixedAmount();
        }
        if (plan.getPromoterPercent() != null && plan.getPromoterPercent().compareTo(ZERO) > 0) {
            return PERCENT_BASE.multiply(plan.getPromoterPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return null;
    }

    private static BigDecimal resolveSalesmanAmount(CommissionPlan plan) {
        if (plan.getSalesmanFixedAmount() != null && plan.getSalesmanFixedAmount().compareTo(ZERO) > 0) {
            return plan.getSalesmanFixedAmount();
        }
        if (plan.getSalesmanPercent() != null && plan.getSalesmanPercent().compareTo(ZERO) > 0) {
            return PERCENT_BASE.multiply(plan.getSalesmanPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return null;
    }

    private static CommissionEntry entry(
            ShopRegistration reg,
            CommissionBeneficiaryType type,
            Long beneficiaryId,
            BigDecimal amount,
            LocalDate periodMonth) {
        CommissionEntry e = new CommissionEntry();
        e.setTenantId(reg.getTenantId());
        e.setShopRegistrationId(reg.getId());
        e.setCommissionEvent(CommissionEvent.LEGACY_SHOP_APPROVAL);
        e.setBeneficiaryType(type);
        e.setBeneficiaryId(beneficiaryId);
        e.setAmount(amount);
        e.setStatus(CommissionEntryStatus.PENDING);
        e.setPeriodMonth(periodMonth);
        e.setCreatedAt(Instant.now());
        return e;
    }

    private static CommissionEntry leadEntry(
            BusinessLead lead,
            CommissionBeneficiaryType type,
            Long beneficiaryId,
            BigDecimal amount,
            LocalDate periodMonth,
            CommissionEvent event) {
        CommissionEntry e = new CommissionEntry();
        e.setTenantId(lead.getTenantId());
        e.setBusinessLeadId(lead.getId());
        e.setCommissionEvent(event);
        e.setBeneficiaryType(type);
        e.setBeneficiaryId(beneficiaryId);
        e.setAmount(amount);
        e.setStatus(CommissionEntryStatus.PENDING);
        e.setPeriodMonth(periodMonth);
        e.setCreatedAt(Instant.now());
        return e;
    }
}
