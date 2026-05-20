package com.shopmanagement.fieldforceservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.shopmanagement.fieldforceservice.model.base.TenantAuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "business_leads")
@Getter
@Setter
public class BusinessLead extends TenantAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String leadCode;

    @Column(nullable = false, length = 300)
    private String businessName;

    @Column(length = 200)
    private String ownerName;

    @Column(nullable = false, length = 20)
    private String mobile;

    @Column(nullable = false, length = 15)
    private String mobileNormalized;

    @Column(length = 20)
    private String alternateMobile;

    @Column(length = 50)
    private String businessType;

    @Column(length = 20)
    private String gstin;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 10)
    private String stateCode;

    @Column(length = 10)
    private String pincode;

    @Column(precision = 10, scale = 7)
    private BigDecimal gpsLatitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal gpsLongitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private LeadSource leadSource = LeadSource.FIELD_VISIT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_promoter_id")
    private Promoter createdByPromoter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_salesman_id")
    private Salesman assignedSalesman;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LeadStatus leadStatus = LeadStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeadPriority priority = LeadPriority.MEDIUM;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private LocalDate expectedConversionDate;

    private Instant convertedAt;

    @Column(length = 64)
    private String externalMerchantId;

    @Column(length = 64)
    private String externalShopId;
}
