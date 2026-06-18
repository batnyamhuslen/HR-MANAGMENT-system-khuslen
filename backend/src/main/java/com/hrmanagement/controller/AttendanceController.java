package com.hrmanagement.controller;

import com.hrmanagement.dto.AttendanceDto;
import com.hrmanagement.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceDto> checkIn(@RequestParam Long employeeId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.checkIn(employeeId));
    }

    @PostMapping("/check-out")
    public ResponseEntity<AttendanceDto> checkOut(@RequestParam Long employeeId) {
        return ResponseEntity.ok(attendanceService.checkOut(employeeId));
    }

    @GetMapping
    public ResponseEntity<List<AttendanceDto>> getByEmployeeAndDateRange(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getByEmployeeAndDateRange(employeeId, from, to));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<AttendanceDto>> getMonthly(
            @RequestParam Long employeeId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(attendanceService.getMonthlyByEmployee(employeeId, year, month));
    }

    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<List<AttendanceDto>> getDaily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(attendanceService.getDailyAttendance(targetDate));
    }
}
