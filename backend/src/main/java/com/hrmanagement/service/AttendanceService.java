package com.hrmanagement.service;

import com.hrmanagement.dto.AttendanceDto;
import com.hrmanagement.entity.Attendance;
import com.hrmanagement.entity.Employee;
import com.hrmanagement.enums.AttendanceStatus;
import com.hrmanagement.repository.AttendanceRepository;
import com.hrmanagement.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    public AttendanceDto checkIn(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        LocalDate today = LocalDate.now();
        if (attendanceRepository.findByEmployeeIdAndDate(employeeId, today).isPresent()) {
            throw new IllegalArgumentException("Already checked in today");
        }

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(today);
        attendance.setCheckInTime(LocalTime.now());
        attendance.setStatus(AttendanceStatus.PRESENT);

        return toDto(attendanceRepository.save(attendance));
    }

    public AttendanceDto checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new IllegalArgumentException("No check-in record found for today"));

        if (attendance.getCheckOutTime() != null) {
            throw new IllegalArgumentException("Already checked out today");
        }

        attendance.setCheckOutTime(LocalTime.now());
        return toDto(attendanceRepository.save(attendance));
    }

    public List<AttendanceDto> getByEmployeeAndDateRange(Long employeeId, LocalDate from, LocalDate to) {
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, from, to).stream()
                .map(this::toDto)
                .toList();
    }

    public List<AttendanceDto> getMonthlyByEmployee(Long employeeId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return attendanceRepository.findMonthlyByEmployee(employeeId, startDate, endDate).stream()
                .map(this::toDto)
                .toList();
    }

    public List<AttendanceDto> getDailyAttendance(LocalDate date) {
        return attendanceRepository.findByDate(date).stream()
                .map(this::toDto)
                .toList();
    }

    private AttendanceDto toDto(Attendance a) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(a.getId());
        dto.setEmployeeId(a.getEmployee().getId());
        dto.setEmployeeName(a.getEmployee().getFirstName() + " " + a.getEmployee().getLastName());
        dto.setDate(a.getDate());
        dto.setCheckInTime(a.getCheckInTime());
        dto.setCheckOutTime(a.getCheckOutTime());
        dto.setStatus(a.getStatus().name());
        dto.setTotalHours(a.getTotalHours());
        dto.setNotes(a.getNotes());
        return dto;
    }
}
