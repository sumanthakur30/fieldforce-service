package com.shopmanagement.fieldforceservice.repository;

import org.springframework.data.jpa.domain.Specification;

import com.shopmanagement.fieldforceservice.model.CommissionBeneficiaryType;
import com.shopmanagement.fieldforceservice.model.CommissionEntry;
import com.shopmanagement.fieldforceservice.model.CommissionEntryStatus;

public final class CommissionEntrySpecifications {

    private CommissionEntrySpecifications() {
    }

    public static Specification<CommissionEntry> search(
            long tenantId,
            CommissionEntryStatus status,
            Long shopRegistrationId,
            CommissionBeneficiaryType beneficiaryType,
            Long beneficiaryId) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (shopRegistrationId != null) {
                predicates.add(cb.equal(root.get("shopRegistrationId"), shopRegistrationId));
            }
            if (beneficiaryType != null) {
                predicates.add(cb.equal(root.get("beneficiaryType"), beneficiaryType));
            }
            if (beneficiaryId != null) {
                predicates.add(cb.equal(root.get("beneficiaryId"), beneficiaryId));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
