package com.shopmanagement.fieldforceservice.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lead_conversions")
@Getter
@Setter
public class LeadConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_lead_id", nullable = false, unique = true)
    private BusinessLead businessLead;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ConversionStage currentStage = ConversionStage.VERIFICATION;

    @Column(length = 64)
    private String subscriptionPlanCode;

    @Column(nullable = false)
    private boolean kycVerified;

    @Column(columnDefinition = "TEXT")
    private String merchantPayloadJson;

    @Column(length = 64)
    private String externalMerchantId;

    @Column(length = 64)
    private String externalShopId;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column(nullable = false)
    private Instant startedAt = Instant.now();

    private Instant completedAt;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();
}
