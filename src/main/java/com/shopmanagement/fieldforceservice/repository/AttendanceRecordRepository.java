package com.shopmanagement.fieldforceservice.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopmanagement.fieldforceservice.model.AttendanceRecord;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByTenantIdAndSalesmanIdAndAttendanceDate(
            long tenantId, Long salesmanId, LocalDate date);

    @Query(
            """
            SELECT a FROM AttendanceRecord a
            WHERE a.tenantId = :tenantId
              AND (:salesmanId IS NULL OR a.salesman.id = :salesmanId)
              AND (:from IS NULL OR a.attendanceDate >= :from)
              AND (:to IS NULL OR a.attendanceDate <= :to)
            ORDER BY a.attendanceDate DESC
            """)
    Page<AttendanceRecord> filter(
            @Param("tenantId") long tenantId,
            @Param("salesmanId") Long salesmanId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable);
}
