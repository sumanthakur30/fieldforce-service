package com.shopmanagement.fieldforceservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.fieldforceservice.model.SalesmanBeatAssignment;

public interface SalesmanBeatAssignmentRepository extends JpaRepository<SalesmanBeatAssignment, Long> {

    List<SalesmanBeatAssignment> findByTenantIdAndSalesmanId(long tenantId, Long salesmanId);

    List<SalesmanBeatAssignment> findByTenantIdAndBeatPlanId(long tenantId, Long beatPlanId);
}
