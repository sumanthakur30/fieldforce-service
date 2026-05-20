package com.shopmanagement.fieldforceservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "commission_plans")
@Getter
@Setter
public class CommissionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 120)
    private String name;

    private BigDecimal promoterFixedAmount;
    private BigDecimal promoterPercent;
    private BigDecimal salesmanFixedAmount;
    private BigDecimal salesmanPercent;

    private BigDecimal leadCreatedAmount;
    private BigDecimal demoCompletedAmount;
    private BigDecimal conversionAmount;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
