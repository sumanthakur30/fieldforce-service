package com.shopmanagement.fieldforceservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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
@Table(name = "field_activities")
@Getter
@Setter
public class FieldActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_lead_id", nullable = false)
    private BusinessLead businessLead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salesman_id")
    private Salesman salesman;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promoter_id")
    private Promoter promoter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FieldActivityType activityType;

    @Column(nullable = false)
    private Instant activityAt = Instant.now();

    @Column(precision = 10, scale = 7)
    private BigDecimal gpsLatitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal gpsLongitude;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDate nextFollowupDate;

    @Column(length = 500)
    private String photoUrl;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant deletedAt;
}
