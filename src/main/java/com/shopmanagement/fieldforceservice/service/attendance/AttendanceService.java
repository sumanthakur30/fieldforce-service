package com.shopmanagement.fieldforceservice.service.attendance;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.AttendanceCheckIn;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.AttendanceCheckOut;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.AttendanceResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.GpsLogCreate;
import com.shopmanagement.fieldforceservice.exception.ConflictException;
import com.shopmanagement.fieldforceservice.exception.NotFoundException;
import com.shopmanagement.fieldforceservice.model.AttendanceRecord;
import com.shopmanagement.fieldforceservice.model.AttendanceStatus;
import com.shopmanagement.fieldforceservice.model.GpsLocationLog;
import com.shopmanagement.fieldforceservice.model.GpsLogSource;
import com.shopmanagement.fieldforceservice.model.Salesman;
import com.shopmanagement.fieldforceservice.repository.AttendanceRecordRepository;
import com.shopmanagement.fieldforceservice.repository.GpsLocationLogRepository;
import com.shopmanagement.fieldforceservice.repository.SalesmanRepository;
import com.shopmanagement.fieldforceservice.support.TenantIds;

@Service
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRepository;
    private final SalesmanRepository salesmanRepository;
    private final GpsLocationLogRepository gpsRepository;

    public AttendanceService(
            AttendanceRecordRepository attendanceRepository,
            SalesmanRepository salesmanRepository,
            GpsLocationLogRepository gpsRepository) {
        this.attendanceRepository = attendanceRepository;
        this.salesmanRepository = salesmanRepository;
        this.gpsRepository = gpsRepository;
    }

    @Transactional
    public AttendanceResponse checkIn(AttendanceCheckIn body) {
        long tenantId = TenantIds.require();
        Salesman salesman = loadSalesman(tenantId, body.salesmanId());
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRepository
                .findByTenantIdAndSalesmanIdAndAttendanceDate(tenantId, salesman.getId(), today)
                .orElseGet(() -> {
                    AttendanceRecord r = new AttendanceRecord();
                    r.setTenantId(tenantId);
                    r.setSalesman(salesman);
                    r.setAttendanceDate(today);
                    return r;
                });
        if (record.getCheckInAt() != null) {
            throw new ConflictException("Already checked in for today");
        }
        record.setCheckInAt(Instant.now());
        record.setCheckInLatitude(body.latitude());
        record.setCheckInLongitude(body.longitude());
        record.setStatus(AttendanceStatus.CHECKED_IN);
        record = attendanceRepository.save(record);
        logGps(tenantId, salesman, body.latitude(), body.longitude(), GpsLogSource.CHECK_IN, null, null);
        return toResponse(record);
    }

    @Transactional
    public AttendanceResponse checkOut(AttendanceCheckOut body) {
        long tenantId = TenantIds.require();
        Salesman salesman = loadSalesman(tenantId, body.salesmanId());
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRepository
                .findByTenantIdAndSalesmanIdAndAttendanceDate(tenantId, salesman.getId(), today)
                .orElseThrow(() -> new NotFoundException("No check-in found for today"));
        if (record.getCheckOutAt() != null) {
            throw new ConflictException("Already checked out for today");
        }
        record.setCheckOutAt(Instant.now());
        record.setCheckOutLatitude(body.latitude());
        record.setCheckOutLongitude(body.longitude());
        record.setStatus(AttendanceStatus.CHECKED_OUT);
        record = attendanceRepository.save(record);
        logGps(tenantId, salesman, body.latitude(), body.longitude(), GpsLogSource.CHECK_OUT, null, null);
        return toResponse(record);
    }

    @Transactional
    public void logLocation(GpsLogCreate body) {
        long tenantId = TenantIds.require();
        Salesman salesman = loadSalesman(tenantId, body.salesmanId());
        logGps(tenantId, salesman, body.latitude(), body.longitude(), GpsLogSource.LIVE_TRACK, body.leadId(), body.fieldActivityId());
    }

    @Transactional(readOnly = true)
    public Page<AttendanceResponse> list(Long salesmanId, LocalDate from, LocalDate to, Pageable pageable) {
        return attendanceRepository
                .filter(TenantIds.require(), salesmanId, from, to, pageable)
                .map(this::toResponse);
    }

    private void logGps(
            long tenantId,
            Salesman salesman,
            java.math.BigDecimal lat,
            java.math.BigDecimal lon,
            GpsLogSource source,
            Long leadId,
            Long activityId) {
        if (lat == null || lon == null) {
            return;
        }
        GpsLocationLog log = new GpsLocationLog();
        log.setTenantId(tenantId);
        log.setSalesman(salesman);
        log.setLatitude(lat);
        log.setLongitude(lon);
        log.setSource(source);
        log.setFieldActivityId(activityId);
        if (leadId != null) {
            com.shopmanagement.fieldforceservice.model.BusinessLead lead = new com.shopmanagement.fieldforceservice.model.BusinessLead();
            lead.setId(leadId);
            log.setBusinessLead(lead);
        }
        gpsRepository.save(log);
    }

    private Salesman loadSalesman(long tenantId, Long id) {
        return salesmanRepository.findByTenantIdAndId(tenantId, id).orElseThrow(() -> new NotFoundException("Salesman not found"));
    }

    private AttendanceResponse toResponse(AttendanceRecord r) {
        return new AttendanceResponse(
                r.getId(),
                r.getSalesman().getId(),
                r.getAttendanceDate(),
                r.getCheckInAt(),
                r.getCheckOutAt(),
                r.getStatus());
    }
}
