package com.shopmanagement.fieldforceservice.model;

import com.shopmanagement.fieldforceservice.model.base.TenantAuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "beat_plans")
@Getter
@Setter
public class BeatPlan extends TenantAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String beatCode;

    @Column(nullable = false, length = 200)
    private String beatName;

    @Column(length = 10)
    private String stateCode;

    @Column(length = 100)
    private String cityCode;

    @Column(columnDefinition = "TEXT")
    private String areaDescription;

    @Column(nullable = false)
    private boolean active = true;
}
