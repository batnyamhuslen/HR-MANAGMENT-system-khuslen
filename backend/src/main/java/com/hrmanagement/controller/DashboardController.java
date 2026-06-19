package com.hrmanagement.controller;

import com.hrmanagement.dto.DashboardStatsDto;
import com.hrmanagement.dto.EmployeeDashboardStatsDto;
import com.hrmanagement.dto.SalaryTrendPointDto;
import com.hrmanagement.enums.UserRole;
import com.hrmanagement.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardStatsDto> getStats(Authentication authentication) {
        UserRole role = UserRole.valueOf(authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("EMPLOYEE"));
        return ResponseEntity.ok(dashboardService.getDashboardStats(role));
    }

    @GetMapping("/my-stats")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<EmployeeDashboardStatsDto> getMyStats() {
        return ResponseEntity.ok(dashboardService.getEmployeeDashboardStats());
    }

    @GetMapping("/salary-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<List<SalaryTrendPointDto>> getSalaryTrend() {
        return ResponseEntity.ok(dashboardService.getLastSixMonthsSalaryTrend());
    }
}
