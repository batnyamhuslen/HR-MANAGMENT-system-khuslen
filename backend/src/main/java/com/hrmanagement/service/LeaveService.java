package com.hrmanagement.service;

import com.hrmanagement.dto.LeaveBalanceDto;
import com.hrmanagement.dto.LeaveRequestDto;
import com.hrmanagement.dto.PendingLeaveRequestDto;
import com.hrmanagement.entity.*;
import com.hrmanagement.enums.LeaveRequestStatus;
import com.hrmanagement.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public LeaveService(LeaveRequestRepository leaveRequestRepository,
                        LeaveBalanceRepository leaveBalanceRepository,
                        LeaveTypeRepository leaveTypeRepository,
                        EmployeeRepository employeeRepository,
                        UserRepository userRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public LeaveRequestDto submitRequest(Long employeeId, Long leaveTypeId, LocalDate startDate,
                                          LocalDate endDate, String reason) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found"));

        int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

        List<LeaveRequest> overlapping = leaveRequestRepository
                .findOverlappingApprovedLeaves(employeeId, startDate, endDate);
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("Employee already has approved leave for this period");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(startDate);
        leaveRequest.setEndDate(endDate);
        leaveRequest.setTotalDays(totalDays);
        leaveRequest.setReason(reason);
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);

        return toDto(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional
    public LeaveRequestDto approveRequest(Long leaveRequestId, Long approverUserId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be approved");
        }

        User approver = userRepository.findById(approverUserId)
                .orElseThrow(() -> new IllegalArgumentException("Approver not found"));

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(
                        leaveRequest.getEmployee().getId(),
                        leaveRequest.getLeaveType().getId(),
                        leaveRequest.getStartDate().getYear())
                .orElseThrow(() -> new IllegalArgumentException("No leave balance found"));

        if (balance.getRemaining() < leaveRequest.getTotalDays()) {
            throw new IllegalArgumentException("Insufficient leave balance");
        }

        leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApprovedAt(LocalDateTime.now());

        balance.setUsed(balance.getUsed() + leaveRequest.getTotalDays());

        leaveBalanceRepository.save(balance);
        return toDto(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional
    public LeaveRequestDto rejectRequest(Long leaveRequestId, Long approverUserId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be rejected");
        }

        User approver = userRepository.findById(approverUserId)
                .orElseThrow(() -> new IllegalArgumentException("Approver not found"));

        leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApprovedAt(LocalDateTime.now());

        return toDto(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional
    public LeaveRequestDto cancelRequest(Long leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (leaveRequest.getStatus() == LeaveRequestStatus.CANCELLED) {
            throw new IllegalArgumentException("Leave request is already cancelled");
        }

        LeaveRequestStatus previousStatus = leaveRequest.getStatus();
        leaveRequest.setStatus(LeaveRequestStatus.CANCELLED);

        if (previousStatus == LeaveRequestStatus.APPROVED) {
            LeaveBalance balance = leaveBalanceRepository
                    .findByEmployeeIdAndLeaveTypeIdAndYear(
                            leaveRequest.getEmployee().getId(),
                            leaveRequest.getLeaveType().getId(),
                            leaveRequest.getStartDate().getYear())
                    .orElse(null);
            if (balance != null) {
                balance.setUsed(balance.getUsed() - leaveRequest.getTotalDays());
                leaveBalanceRepository.save(balance);
            }
        }

        return toDto(leaveRequestRepository.save(leaveRequest));
    }

    public List<PendingLeaveRequestDto> getPendingRequests(int limit) {
        return leaveRequestRepository.findPendingRequests(PageRequest.of(0, limit)).stream()
                .map(lr -> {
                    String name = lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName();
                    String initials = String.valueOf(lr.getEmployee().getFirstName().charAt(0)) +
                            lr.getEmployee().getLastName().charAt(0);
                    return new PendingLeaveRequestDto(
                            lr.getId(), name, initials,
                            lr.getLeaveType().getName(), lr.getTotalDays());
                })
                .toList();
    }

    public List<LeaveRequestDto> getEmployeeRequests(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<LeaveBalanceDto> getEmployeeBalances(Long employeeId) {
        return leaveBalanceRepository.findByEmployeeId(employeeId).stream()
                .map(this::toBalanceDto)
                .toList();
    }

    public long getEmployeesOnLeaveToday() {
        LocalDate today = LocalDate.now();
        return leaveRequestRepository.countByStatusAndDateRange("APPROVED", today, today);
    }

    private LeaveRequestDto toDto(LeaveRequest lr) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(lr.getId());
        dto.setEmployeeId(lr.getEmployee().getId());
        dto.setEmployeeName(lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName());
        dto.setLeaveTypeId(lr.getLeaveType().getId());
        dto.setLeaveTypeName(lr.getLeaveType().getName());
        dto.setStartDate(lr.getStartDate());
        dto.setEndDate(lr.getEndDate());
        dto.setTotalDays(lr.getTotalDays());
        dto.setReason(lr.getReason());
        dto.setStatus(lr.getStatus().name());
        if (lr.getApprovedBy() != null) {
            dto.setApprovedById(lr.getApprovedBy().getId());
            dto.setApprovedByName(lr.getApprovedBy().getUsername());
        }
        dto.setApprovedAt(lr.getApprovedAt());
        dto.setCreatedAt(lr.getCreatedAt());
        return dto;
    }

    private LeaveBalanceDto toBalanceDto(LeaveBalance lb) {
        LeaveBalanceDto dto = new LeaveBalanceDto();
        dto.setId(lb.getId());
        dto.setEmployeeId(lb.getEmployee().getId());
        dto.setLeaveTypeId(lb.getLeaveType().getId());
        dto.setLeaveTypeName(lb.getLeaveType().getName());
        dto.setYear(lb.getYear());
        dto.setTotalAllocated(lb.getTotalAllocated());
        dto.setUsed(lb.getUsed());
        dto.setRemaining(lb.getRemaining());
        return dto;
    }
}
