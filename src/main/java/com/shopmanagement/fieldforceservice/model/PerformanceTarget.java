package com.shopmanagement.fieldforceservice.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.shopmanagement.fieldforceservice.model.base.TenantAuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "performance_targets")
@Getter
@Setter
public class PerformanceTarget extends TenantAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate targetMonth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promoter_id")
    private Promoter promoter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salesman_id")
    private Salesman salesman;

    @Column(nullable = false)
    private int leadTarget;

    @Column(nullable = false)
    private int conversionTarget;

    @Column(precision = 14, scale = 2)
    private BigDecimal revenueTarget;
}
