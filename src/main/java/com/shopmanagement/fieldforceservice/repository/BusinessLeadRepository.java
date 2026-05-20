package com.shopmanagement.fieldforceservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopmanagement.fieldforceservice.model.BusinessLead;
import com.shopmanagement.fieldforceservice.model.LeadStatus;

public interface BusinessLeadRepository extends JpaRepository<BusinessLead, Long> {

    Optional<BusinessLead> findByTenantIdAndIdAndDeletedAtIsNull(Long tenantId, Long id);

    Optional<BusinessLead> findByTenantIdAndLeadCodeAndDeletedAtIsNull(Long tenantId, String leadCode);

    List<BusinessLead> findByTenantIdAndMobileNormalizedAndDeletedAtIsNull(Long tenantId, String mobileNormalized);

    List<BusinessLead> findByTenantIdAndGstinIgnoreCaseAndDeletedAtIsNull(Long tenantId, String gstin);

    @Query(
            """
            SELECT l FROM BusinessLead l
            WHERE l.tenantId = :tenantId AND l.deletedAt IS NULL
              AND (:status IS NULL OR l.leadStatus = :status)
              AND (:promoterId IS NULL OR l.createdByPromoter.id = :promoterId)
              AND (:salesmanId IS NULL OR l.assignedSalesman.id = :salesmanId)
              AND (:state IS NULL OR l.stateCode = :state)
              AND (:city IS NULL OR LOWER(l.city) LIKE LOWER(CONCAT('%', :city, '%')))
              AND (:q IS NULL OR LOWER(l.businessName) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR l.mobile LIKE CONCAT('%', :q, '%')
                   OR l.leadCode LIKE CONCAT('%', :q, '%'))
            """)
    Page<BusinessLead> search(
            @Param("tenantId") long tenantId,
            @Param("status") LeadStatus status,
            @Param("promoterId") Long promoterId,
            @Param("salesmanId") Long salesmanId,
            @Param("state") String state,
            @Param("city") String city,
            @Param("q") String q,
            Pageable pageable);

    @Query(
            """
            SELECT l.leadStatus, COUNT(l) FROM BusinessLead l
            WHERE l.tenantId = :tenantId AND l.deletedAt IS NULL
            GROUP BY l.leadStatus
            """)
    List<Object[]> countByStatus(@Param("tenantId") long tenantId);

    long countByTenantIdAndDeletedAtIsNull(long tenantId);

    long countByTenantIdAndLeadStatusAndDeletedAtIsNull(long tenantId, LeadStatus status);

    long countByTenantIdAndCreatedByPromoterIdAndDeletedAtIsNull(long tenantId, Long promoterId);

    long countByTenantIdAndAssignedSalesmanIdAndLeadStatusAndDeletedAtIsNull(
            long tenantId, Long salesmanId, LeadStatus status);
}
