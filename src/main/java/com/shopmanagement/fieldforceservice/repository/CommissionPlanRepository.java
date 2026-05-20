package com.shopmanagement.fieldforceservice.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopmanagement.fieldforceservice.model.CommissionPlan;

public interface CommissionPlanRepository extends JpaRepository<CommissionPlan, Long> {

    List<CommissionPlan> findByTenantIdAndActiveTrueOrderByEffectiveFromDesc(Long tenantId);

    @Query("""
            SELECT p FROM CommissionPlan p WHERE p.tenantId = :tenantId AND p.active = true
            AND p.effectiveFrom <= :onDate
            AND (p.effectiveTo IS NULL OR p.effectiveTo >= :onDate)
            ORDER BY p.effectiveFrom DESC
            """)
    List<CommissionPlan> findApplicable(@Param("tenantId") Long tenantId, @Param("onDate") LocalDate onDate);
}
