package com.shopmanagement.fieldforceservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.fieldforceservice.model.GpsLocationLog;

public interface GpsLocationLogRepository extends JpaRepository<GpsLocationLog, Long> {

    Page<GpsLocationLog> findByTenantIdAndSalesmanIdOrderByRecordedAtDesc(
            long tenantId, Long salesmanId, Pageable pageable);
}
