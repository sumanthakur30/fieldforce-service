package com.shopmanagement.fieldforceservice.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopmanagement.fieldforceservice.model.FieldActivity;
import com.shopmanagement.fieldforceservice.model.FieldActivityType;

public interface FieldActivityRepository extends JpaRepository<FieldActivity, Long> {

    List<FieldActivity> findByTenantIdAndBusinessLeadIdAndDeletedAtIsNullOrderByActivityAtDesc(
            long tenantId, Long leadId);

    Page<FieldActivity> findByTenantIdAndDeletedAtIsNullOrderByActivityAtDesc(long tenantId, Pageable pageable);

    @Query(
            """
            SELECT a FROM FieldActivity a
            WHERE a.tenantId = :tenantId AND a.deletedAt IS NULL
              AND (:leadId IS NULL OR a.businessLead.id = :leadId)
              AND (:salesmanId IS NULL OR a.salesman.id = :salesmanId)
              AND (:type IS NULL OR a.activityType = :type)
              AND (:from IS NULL OR a.activityAt >= :from)
              AND (:to IS NULL OR a.activityAt <= :to)
            ORDER BY a.activityAt DESC
            """)
    Page<FieldActivity> filter(
            @Param("tenantId") long tenantId,
            @Param("leadId") Long leadId,
            @Param("salesmanId") Long salesmanId,
            @Param("type") FieldActivityType type,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);

    boolean existsByTenantIdAndBusinessLeadIdAndActivityTypeAndDeletedAtIsNull(
            long tenantId, Long leadId, FieldActivityType type);
}
