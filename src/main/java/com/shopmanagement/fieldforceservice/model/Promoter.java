package com.shopmanagement.fieldforceservice.model;

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
@Table(name = "promoters")
@Getter
@Setter
public class Promoter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 32)
    private String promoterCode;

    @Column(nullable = false, length = 200)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String mobile;

    @Column(length = 200)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, length = 10)
    private String stateCode;

    @Column(nullable = false, length = 100)
    private String cityCode;

    @Column(length = 500)
    private String profilePhotoUrl;

    @Column(nullable = false)
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FieldPersonStatus status = FieldPersonStatus.ACTIVE;

    @Column(length = 20)
    private String pan;

    @Column(length = 20)
    private String aadhaarMasked;

    @Column(length = 20)
    private String gstin;

    @Column(length = 200)
    private String bankAccountName;

    @Column(length = 20)
    private String bankIfsc;

    @Column(length = 40)
    private String bankAccountNumber;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();
}
