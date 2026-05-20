package com.shopmanagement.fieldforceservice.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopmanagement.fieldforceservice.model.FieldPersonStatus;
import com.shopmanagement.fieldforceservice.model.Promoter;

public interface PromoterRepository extends JpaRepository<Promoter, Long> {

    Optional<Promoter> findByTenantIdAndId(Long tenantId, Long id);

    Page<Promoter> findByTenantId(Long tenantId, Pageable pageable);

    @Query("""
            SELECT p FROM Promoter p WHERE p.tenantId = :tenantId
            AND (:state IS NULL OR p.stateCode = :state)
            AND (:city IS NULL OR p.cityCode = :city)
            AND (:status IS NULL OR p.status = :status)
            AND (:q IS NULL OR :q = ''
                OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.mobile LIKE CONCAT('%', :q, '%'))
            """)
    Page<Promoter> search(
            @Param("tenantId") Long tenantId,
            @Param("state") String state,
            @Param("city") String city,
            @Param("status") FieldPersonStatus status,
            @Param("q") String q,
            Pageable pageable);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, FieldPersonStatus status);
}
