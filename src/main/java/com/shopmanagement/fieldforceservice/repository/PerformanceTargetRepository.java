package com.shopmanagement.fieldforceservice.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.fieldforceservice.model.PerformanceTarget;

public interface PerformanceTargetRepository extends JpaRepository<PerformanceTarget, Long> {

    Optional<PerformanceTarget> findByTenantIdAndTargetMonthAndPromoterIdAndSalesmanId(
            long tenantId, LocalDate targetMonth, Long promoterId, Long salesmanId);
}
