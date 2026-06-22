package com.hrmanagement.controller;

import com.hrmanagement.dto.payroll.PayrollRecordDto;
import com.hrmanagement.entity.Employee;
import com.hrmanagement.entity.User;
import com.hrmanagement.repository.EmployeeRepository;
import com.hrmanagement.repository.UserRepository;
import com.hrmanagement.service.PayrollService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService payrollService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public PayrollController(PayrollService payrollService,
                             UserRepository userRepository,
                             EmployeeRepository employeeRepository) {
        this.payrollService = payrollService;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping("/run")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Map<String, Object>> runPayroll(
            @RequestParam int year, @RequestParam int month) {
        Map<String, Object> summary = payrollService.runPayrollForAllActiveEmployees(year, month);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Page<PayrollRecordDto>> getRecords(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(payrollService.getRecordsByPeriod(year, month, pageable));
    }

    @GetMapping("/records/employee/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PayrollRecordDto>> getEmployeeHistory(
            @PathVariable Long employeeId, Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        if (role.equals("ROLE_EMPLOYEE")) {
            Long currentEmpId = getCurrentEmployeeId(auth.getName());
            if (!currentEmpId.equals(employeeId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(payrollService.getEmployeeHistory(employeeId));
    }

    @GetMapping("/my-records")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<PayrollRecordDto>> getMyRecords(Authentication auth) {
        Long empId = getCurrentEmployeeId(auth.getName());
        return ResponseEntity.ok(payrollService.getEmployeeHistory(empId));
    }

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<PayrollRecordDto> finalizePayroll(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.finalizePayroll(id));
    }

    @GetMapping("/{id}/payslip")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PayrollRecordDto> getPayslip(@PathVariable Long id, Authentication auth) {
        PayrollRecordDto dto = payrollService.getPayslip(id);
        String role = auth.getAuthorities().iterator().next().getAuthority();
        if (role.equals("ROLE_EMPLOYEE")) {
            Long currentEmpId = getCurrentEmployeeId(auth.getName());
            if (!currentEmpId.equals(dto.getEmployeeId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(dto);
    }

    private Long getCurrentEmployeeId(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        Employee employee = employeeRepository.findAll().stream()
                .filter(e -> e.getUser() != null && e.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No employee linked to user: " + username));
        return employee.getId();
    }
}
