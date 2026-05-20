package com.shopmanagement.fieldforceservice.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopmanagement.fieldforceservice.model.ApprovalStatus;
import com.shopmanagement.fieldforceservice.model.Promoter;
import com.shopmanagement.fieldforceservice.model.Salesman;
import com.shopmanagement.fieldforceservice.model.ShopRegistration;

public interface ShopRegistrationRepository extends JpaRepository<ShopRegistration, Long> {

    Optional<ShopRegistration> findByTenantIdAndExternalShopId(Long tenantId, String externalShopId);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndPromoter(Long tenantId, Promoter promoter);

    long countByTenantIdAndSalesman(Long tenantId, Salesman salesman);

    long countByTenantIdAndApprovalStatus(Long tenantId, ApprovalStatus approvalStatus);

    @Query("""
            SELECT COUNT(r) FROM ShopRegistration r WHERE r.tenantId = :tenantId
            AND r.registeredAt >= :from AND r.registeredAt < :to
            """)
    long countRegisteredBetween(
            @Param("tenantId") Long tenantId, @Param("from") Instant from, @Param("to") Instant to);

    Page<ShopRegistration> findByTenantId(Long tenantId, Pageable pageable);

    Page<ShopRegistration> findByTenantIdAndPromoter(Long tenantId, Promoter promoter, Pageable pageable);

    Page<ShopRegistration> findByTenantIdAndSalesman(Long tenantId, Salesman salesman, Pageable pageable);

    @Query("""
            SELECT r FROM ShopRegistration r WHERE r.tenantId = :tenantId
            AND (:state IS NULL OR r.stateCode = :state)
            AND (:city IS NULL OR r.cityCode = :city)
            AND (:status IS NULL OR r.approvalStatus = :status)
            AND (:promoterId IS NULL OR (r.promoter IS NOT NULL AND r.promoter.id = :promoterId))
            AND (:salesmanId IS NULL OR (r.salesman IS NOT NULL AND r.salesman.id = :salesmanId))
            """)
    Page<ShopRegistration> filter(
            @Param("tenantId") Long tenantId,
            @Param("state") String state,
            @Param("city") String city,
            @Param("status") ApprovalStatus status,
            @Param("promoterId") Long promoterId,
            @Param("salesmanId") Long salesmanId,
            Pageable pageable);
}
