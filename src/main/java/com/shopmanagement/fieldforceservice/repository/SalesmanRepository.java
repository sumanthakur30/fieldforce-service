package com.shopmanagement.fieldforceservice.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopmanagement.fieldforceservice.model.FieldPersonStatus;
import com.shopmanagement.fieldforceservice.model.Promoter;
import com.shopmanagement.fieldforceservice.model.Salesman;

public interface SalesmanRepository extends JpaRepository<Salesman, Long> {

    Optional<Salesman> findByTenantIdAndId(Long tenantId, Long id);

    Page<Salesman> findByTenantIdAndPromoter(Long tenantId, Promoter promoter, Pageable pageable);

    @Query("""
            SELECT s FROM Salesman s WHERE s.tenantId = :tenantId
            AND (:promoterId IS NULL OR s.promoter.id = :promoterId)
            AND (:state IS NULL OR s.stateCode = :state)
            AND (:city IS NULL OR s.cityCode = :city)
            AND (:status IS NULL OR s.status = :status)
            AND (:q IS NULL OR :q = ''
                OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
                OR s.mobile LIKE CONCAT('%', :q, '%'))
            """)
    Page<Salesman> search(
            @Param("tenantId") Long tenantId,
            @Param("promoterId") Long promoterId,
            @Param("state") String state,
            @Param("city") String city,
            @Param("status") FieldPersonStatus status,
            @Param("q") String q,
            Pageable pageable);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndPromoter(Long tenantId, Promoter promoter);
}
