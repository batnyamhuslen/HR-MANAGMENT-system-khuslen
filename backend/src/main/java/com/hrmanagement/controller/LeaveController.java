package com.hrmanagement.controller;

import com.hrmanagement.dto.LeaveBalanceDto;
import com.hrmanagement.dto.LeaveRequestDto;
import com.hrmanagement.dto.PendingLeaveRequestDto;
import com.hrmanagement.repository.UserRepository;
import com.hrmanagement.service.LeaveService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveController {

    private final LeaveService leaveService;
    private final UserRepository userRepository;

    public LeaveController(LeaveService leaveService, UserRepository userRepository) {
        this.leaveService = leaveService;
        this.userRepository = userRepository;
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<List<PendingLeaveRequestDto>> getPending(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(leaveService.getPendingRequests(limit));
    }

    @PostMapping
    public ResponseEntity<LeaveRequestDto> submit(
            @RequestParam Long employeeId,
            @RequestParam Long leaveTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveService.submitRequest(employeeId, leaveTypeId, startDate, endDate, reason));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<LeaveRequestDto> approve(@PathVariable Long id, Principal principal) {
        com.hrmanagement.entity.User approver = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(leaveService.approveRequest(id, approver.getId()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<LeaveRequestDto> reject(@PathVariable Long id, Principal principal) {
        com.hrmanagement.entity.User approver = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(leaveService.rejectRequest(id, approver.getId()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<LeaveRequestDto> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.cancelRequest(id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LeaveRequestDto>> getMyRequests(Principal principal) {
        com.hrmanagement.entity.User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(leaveService.getEmployeeRequests(user.getId()));
    }

    @GetMapping("/balances")
    public ResponseEntity<List<LeaveBalanceDto>> getMyBalances(Principal principal) {
        com.hrmanagement.entity.User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(leaveService.getEmployeeBalances(user.getId()));
    }
}
