package com.shopmanagement.fieldforceservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "commission_entries")
@Getter
@Setter
public class CommissionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    private Long shopRegistrationId;

    private Long businessLeadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CommissionEvent commissionEvent = CommissionEvent.LEGACY_SHOP_APPROVAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommissionBeneficiaryType beneficiaryType;

    @Column(nullable = false)
    private Long beneficiaryId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommissionEntryStatus status = CommissionEntryStatus.PENDING;

    private LocalDate periodMonth;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant paidAt;

    @Column(length = 120)
    private String payoutReference;
}
