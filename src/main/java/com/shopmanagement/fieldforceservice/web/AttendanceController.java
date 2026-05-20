package com.shopmanagement.fieldforceservice.web;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.AttendanceCheckIn;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.AttendanceCheckOut;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.AttendanceResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.GpsLogCreate;
import com.shopmanagement.fieldforceservice.service.attendance.AttendanceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse checkIn(@Valid @RequestBody AttendanceCheckIn body) {
        return attendanceService.checkIn(body);
    }

    @PostMapping("/check-out")
    public AttendanceResponse checkOut(@Valid @RequestBody AttendanceCheckOut body) {
        return attendanceService.checkOut(body);
    }

    @PostMapping("/gps")
    @ResponseStatus(HttpStatus.CREATED)
    public void logGps(@Valid @RequestBody GpsLogCreate body) {
        attendanceService.logLocation(body);
    }

    @GetMapping
    public Page<AttendanceResponse> list(
            @RequestParam(required = false) Long salesmanId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Pageable pageable) {
        return attendanceService.list(salesmanId, from, to, pageable);
    }
}
