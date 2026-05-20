package com.shopmanagement.fieldforceservice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopmanagement.fieldforceservice.model.CommissionBeneficiaryType;
import com.shopmanagement.fieldforceservice.model.CommissionEntry;
import com.shopmanagement.fieldforceservice.model.CommissionEntryStatus;
import com.shopmanagement.fieldforceservice.model.CommissionEvent;

public interface CommissionEntryRepository extends JpaRepository<CommissionEntry, Long>, JpaSpecificationExecutor<CommissionEntry> {

    boolean existsByShopRegistrationId(Long shopRegistrationId);

    boolean existsByTenantIdAndBusinessLeadIdAndCommissionEvent(
            Long tenantId, Long businessLeadId, CommissionEvent event);

    List<CommissionEntry> findByTenantIdAndBeneficiaryTypeAndBeneficiaryIdAndStatus(
            Long tenantId, CommissionBeneficiaryType type, Long beneficiaryId, CommissionEntryStatus status);

    List<CommissionEntry> findByTenantIdAndShopRegistrationId(Long tenantId, Long shopRegistrationId);

    @org.springframework.data.jpa.repository.Query("""
            SELECT COALESCE(SUM(e.amount), 0) FROM CommissionEntry e
            WHERE e.tenantId = :tenantId AND e.beneficiaryType = :bt AND e.beneficiaryId = :bid AND e.status = :st
            """)
    java.math.BigDecimal sumAmount(
            @org.springframework.data.repository.query.Param("tenantId") Long tenantId,
            @org.springframework.data.repository.query.Param("bt") CommissionBeneficiaryType beneficiaryType,
            @org.springframework.data.repository.query.Param("bid") Long beneficiaryId,
            @org.springframework.data.repository.query.Param("st") CommissionEntryStatus status);

    @org.springframework.data.jpa.repository.Query("""
            SELECT COUNT(e) FROM CommissionEntry e WHERE e.tenantId = :tenantId AND e.status = :st
            """)
    long countByTenantIdAndStatus(
            @org.springframework.data.repository.query.Param("tenantId") Long tenantId,
            @org.springframework.data.repository.query.Param("st") CommissionEntryStatus status);

    @org.springframework.data.jpa.repository.Query("""
            SELECT COALESCE(SUM(e.amount), 0) FROM CommissionEntry e
            WHERE e.tenantId = :tenantId AND e.status = :st
            """)
    java.math.BigDecimal sumAmountByTenantAndStatus(
            @org.springframework.data.repository.query.Param("tenantId") Long tenantId,
            @org.springframework.data.repository.query.Param("st") CommissionEntryStatus status);
}
