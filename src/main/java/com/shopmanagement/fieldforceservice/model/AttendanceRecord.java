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
@Table(name = "attendance_records")
@Getter
@Setter
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salesman_id", nullable = false)
    private Salesman salesman;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    private Instant checkInAt;

    private Instant checkOutAt;

    @Column(precision = 10, scale = 7)
    private BigDecimal checkInLatitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal checkInLongitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal checkOutLatitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal checkOutLongitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status = AttendanceStatus.CHECKED_IN;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
