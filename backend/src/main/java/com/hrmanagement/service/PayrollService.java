package com.hrmanagement.service;

import com.hrmanagement.dto.payroll.PayrollRecordDto;
import com.hrmanagement.entity.*;
import com.hrmanagement.enums.EmployeeStatus;
import com.hrmanagement.enums.PayrollStatus;
import com.hrmanagement.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PayrollService {

    private final PayrollConfigRepository payrollConfigRepository;
    private final PayrollRecordRepository payrollRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    private final Map<String, BigDecimal> configCache = new ConcurrentHashMap<>();

    public PayrollService(PayrollConfigRepository payrollConfigRepository,
                          PayrollRecordRepository payrollRecordRepository,
                          EmployeeRepository employeeRepository,
                          AttendanceRepository attendanceRepository,
                          LeaveRequestRepository leaveRequestRepository) {
        this.payrollConfigRepository = payrollConfigRepository;
        this.payrollRecordRepository = payrollRecordRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    @PostConstruct
    public void loadConfigCache() {
        List<PayrollConfig> all = payrollConfigRepository.findAll();
        for (PayrollConfig cfg : all) {
            configCache.put(cfg.getConfigKey(), cfg.getConfigValue());
        }
    }

    public void reloadConfigCache() {
        configCache.clear();
        loadConfigCache();
    }

    private BigDecimal getConfig(String key) {
        BigDecimal val = configCache.get(key);
        if (val == null) {
            throw new IllegalStateException("Payroll config key not found: " + key);
        }
        return val;
    }

    @Transactional
    public PayrollRecordDto calculatePayroll(Long employeeId, int year, int month) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        BigDecimal baseSalary = employee.getSalary();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();
        int workingDays = countWorkingDays(yearMonth);

        // 2. Overtime calculation
        // Treat any totalHours beyond 8 hours/day as overtime
        List<Attendance> attendances = attendanceRepository.findMonthlyByEmployee(employeeId, monthStart, monthEnd);
        BigDecimal overtimeHours = BigDecimal.ZERO;
        for (Attendance a : attendances) {
            if (a.getTotalHours() != null && a.getTotalHours().compareTo(BigDecimal.valueOf(8)) > 0) {
                overtimeHours = overtimeHours.add(a.getTotalHours().subtract(BigDecimal.valueOf(8)));
            }
        }

        BigDecimal hourlyRate = baseSalary.divide(BigDecimal.valueOf((long) workingDays * 8), 10, RoundingMode.HALF_UP);
        BigDecimal overtimePay = overtimeHours.multiply(hourlyRate).multiply(getConfig("OVERTIME_MULTIPLIER"))
                .setScale(2, RoundingMode.HALF_UP);

        // 3. Unpaid leave deduction
        List<LeaveRequest> unpaidLeaves = leaveRequestRepository
                .findOverlappingApprovedLeaveByType(employeeId, "Unpaid", monthStart, monthEnd);
        BigDecimal unpaidLeaveDeduction = BigDecimal.ZERO;
        if (!unpaidLeaves.isEmpty()) {
            BigDecimal dailyRate = baseSalary.divide(BigDecimal.valueOf(workingDays), 10, RoundingMode.HALF_UP);
            for (LeaveRequest lr : unpaidLeaves) {
                LocalDate overlapStart = lr.getStartDate().isBefore(monthStart) ? monthStart : lr.getStartDate();
                LocalDate overlapEnd = lr.getEndDate().isAfter(monthEnd) ? monthEnd : lr.getEndDate();
                if (!overlapStart.isAfter(overlapEnd)) {
                    long overlapDays = ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
                    unpaidLeaveDeduction = unpaidLeaveDeduction.add(
                            dailyRate.multiply(BigDecimal.valueOf(overlapDays)));
                }
            }
            unpaidLeaveDeduction = unpaidLeaveDeduction.setScale(2, RoundingMode.HALF_UP);
        }

        // 4. Allowances = 0 for now
        // TODO: Add an allowances field to Employee or a separate employee_allowances table
        BigDecimal allowances = BigDecimal.ZERO;

        // 5. Gross salary
        BigDecimal grossSalary = baseSalary.add(overtimePay).add(allowances).subtract(unpaidLeaveDeduction);

        // 6. Social insurance
        BigDecimal siCap = getConfig("SOCIAL_INSURANCE_CAP");
        BigDecimal siBase = grossSalary.min(siCap);
        BigDecimal siEmployee = siBase.multiply(getConfig("SOCIAL_INSURANCE_EMPLOYEE_RATE"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal siEmployer = siBase.multiply(getConfig("SOCIAL_INSURANCE_EMPLOYER_RATE"))
                .setScale(2, RoundingMode.HALF_UP);

        // 7. Taxable income
        BigDecimal taxableIncome = grossSalary.subtract(siEmployee);

        // 8. Income tax — flat 10% per current law
        // TODO: Implement progressive tax bracket table when law changes
        BigDecimal incomeTax = taxableIncome.multiply(getConfig("INCOME_TAX_RATE"))
                .setScale(2, RoundingMode.HALF_UP);

        // 9. Net salary
        BigDecimal otherDeductions = BigDecimal.ZERO;
        BigDecimal netSalary = grossSalary.subtract(siEmployee).subtract(incomeTax).subtract(otherDeductions);

        // Check for existing DRAFT record
        Optional<PayrollRecord> existing = payrollRecordRepository
                .findByEmployeeIdAndPayPeriodYearAndPayPeriodMonth(employeeId, year, month);

        PayrollRecord record;
        if (existing.isPresent()) {
            record = existing.get();
            if (record.getStatus() != PayrollStatus.DRAFT) {
                throw new IllegalStateException(
                        "Cannot recalculate a " + record.getStatus() + " payroll record. " +
                        "Only DRAFT records can be overwritten.");
            }
        } else {
            record = new PayrollRecord();
            record.setEmployee(employee);
            record.setPayPeriodYear(year);
            record.setPayPeriodMonth(month);
        }

        record.setBaseSalary(baseSalary);
        record.setOvertimeHours(overtimeHours);
        record.setOvertimePay(overtimePay);
        record.setAllowances(allowances);
        record.setUnpaidLeaveDeduction(unpaidLeaveDeduction);
        record.setGrossSalary(grossSalary);
        record.setSocialInsuranceEmployee(siEmployee);
        record.setSocialInsuranceEmployer(siEmployer);
        record.setTaxableIncome(taxableIncome);
        record.setIncomeTax(incomeTax);
        record.setOtherDeductions(otherDeductions);
        record.setNetSalary(netSalary);

        PayrollRecord saved = payrollRecordRepository.save(record);
        return toDto(saved);
    }

    @Transactional
    public PayrollRecordDto recalculate(Long payrollId) {
        PayrollRecord record = payrollRecordRepository.findById(payrollId)
                .orElseThrow(() -> new IllegalArgumentException("Payroll record not found: " + payrollId));
        if (record.getStatus() != PayrollStatus.DRAFT) {
            throw new IllegalStateException(
                    "Cannot recalculate a " + record.getStatus() + " payroll record. " +
                    "Only DRAFT records can be overwritten.");
        }
        return calculatePayroll(record.getEmployee().getId(), record.getPayPeriodYear(), record.getPayPeriodMonth());
    }

    @Transactional
    public PayrollRecordDto finalizePayroll(Long payrollId) {
        PayrollRecord record = payrollRecordRepository.findById(payrollId)
                .orElseThrow(() -> new IllegalArgumentException("Payroll record not found: " + payrollId));
        if (record.getStatus() != PayrollStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT records can be finalized. Current status: " + record.getStatus());
        }
        record.setStatus(PayrollStatus.FINALIZED);
        record.setFinalizedAt(java.time.LocalDateTime.now());
        return toDto(payrollRecordRepository.save(record));
    }

    @Transactional
    public Map<String, Object> runPayrollForAllActiveEmployees(int year, int month) {
        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .toList();

        int processedCount = 0;
        int skippedCount = 0;
        BigDecimal totalNetPayout = BigDecimal.ZERO;

        for (Employee emp : activeEmployees) {
            Optional<PayrollRecord> existing = payrollRecordRepository
                    .findByEmployeeIdAndPayPeriodYearAndPayPeriodMonth(emp.getId(), year, month);
            if (existing.isPresent() && existing.get().getStatus() != PayrollStatus.DRAFT) {
                skippedCount++;
                continue;
            }
            PayrollRecordDto dto = calculatePayroll(emp.getId(), year, month);
            processedCount++;
            totalNetPayout = totalNetPayout.add(dto.getNetSalary());
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("processedCount", processedCount);
        summary.put("skippedCount", skippedCount);
        summary.put("totalNetPayout", totalNetPayout);
        return summary;
    }

    public Page<PayrollRecordDto> getRecordsByPeriod(int year, int month, Pageable pageable) {
        return payrollRecordRepository
                .findByPayPeriodYearAndPayPeriodMonth(year, month, pageable)
                .map(this::toDto);
    }

    public List<PayrollRecordDto> getEmployeeHistory(Long employeeId) {
        return payrollRecordRepository
                .findByEmployeeIdOrderByPayPeriodYearDescPayPeriodMonthDesc(employeeId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public PayrollRecordDto getPayslip(Long payrollId) {
        PayrollRecord record = payrollRecordRepository.findById(payrollId)
                .orElseThrow(() -> new IllegalArgumentException("Payroll record not found: " + payrollId));
        return toDto(record);
    }

    private PayrollRecordDto toDto(PayrollRecord record) {
        PayrollRecordDto dto = new PayrollRecordDto();
        dto.setId(record.getId());
        dto.setEmployeeId(record.getEmployee().getId());
        String name = record.getEmployee().getLastName() + " " + record.getEmployee().getFirstName();
        dto.setEmployeeName(name.trim());
        dto.setPayPeriodYear(record.getPayPeriodYear());
        dto.setPayPeriodMonth(record.getPayPeriodMonth());
        dto.setBaseSalary(record.getBaseSalary());
        dto.setOvertimePay(record.getOvertimePay());
        dto.setAllowances(record.getAllowances());
        dto.setUnpaidLeaveDeduction(record.getUnpaidLeaveDeduction());
        dto.setGrossSalary(record.getGrossSalary());
        dto.setSocialInsuranceEmployee(record.getSocialInsuranceEmployee());
        dto.setIncomeTax(record.getIncomeTax());
        dto.setOtherDeductions(record.getOtherDeductions());
        dto.setNetSalary(record.getNetSalary());
        dto.setStatus(record.getStatus().name());
        return dto;
    }

    private int countWorkingDays(YearMonth yearMonth) {
        int days = 0;
        LocalDate date = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        while (!date.isAfter(end)) {
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                days++;
            }
            date = date.plusDays(1);
        }
        return days;
    }
}
