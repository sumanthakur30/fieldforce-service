package com.shopmanagement.fieldforceservice.model;

import java.time.Instant;

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
@Table(name = "promoter_territories")
@Getter
@Setter
public class PromoterTerritory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promoter_id")
    private Promoter promoter;

    @Column(nullable = false, length = 10)
    private String stateCode;

    @Column(nullable = false, length = 100)
    private String cityCode;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
