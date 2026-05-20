package com.shopmanagement.fieldforceservice.model;

import java.math.BigDecimal;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "gps_location_logs")
@Getter
@Setter
public class GpsLocationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salesman_id", nullable = false)
    private Salesman salesman;

    @Column(nullable = false)
    private Instant recordedAt = Instant.now();

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(precision = 8, scale = 2)
    private BigDecimal accuracyMeters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GpsLogSource source = GpsLogSource.LIVE_TRACK;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_lead_id")
    private BusinessLead businessLead;

    @Column(name = "field_activity_id")
    private Long fieldActivityId;
}
