package com.shopmanagement.fieldforceservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.fieldforceservice.model.LeadConversion;

public interface LeadConversionRepository extends JpaRepository<LeadConversion, Long> {

    Optional<LeadConversion> findByTenantIdAndBusinessLeadId(long tenantId, Long leadId);
}
