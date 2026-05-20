package com.shopmanagement.fieldforceservice.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopmanagement.fieldforceservice.model.BeatPlan;

public interface BeatPlanRepository extends JpaRepository<BeatPlan, Long> {

    Optional<BeatPlan> findByTenantIdAndIdAndDeletedAtIsNull(Long tenantId, Long id);

    @Query(
            """
            SELECT b FROM BeatPlan b
            WHERE b.tenantId = :tenantId AND b.deletedAt IS NULL
              AND (:active IS NULL OR b.active = :active)
              AND (:q IS NULL OR LOWER(b.beatName) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR b.beatCode LIKE CONCAT('%', :q, '%'))
            """)
    Page<BeatPlan> search(
            @Param("tenantId") long tenantId,
            @Param("active") Boolean active,
            @Param("q") String q,
            Pageable pageable);
}
