package com.hrmanagement.service;

import com.hrmanagement.dto.DashboardStatsDto;
import com.hrmanagement.dto.EmployeeDashboardStatsDto;
import com.hrmanagement.dto.SalaryTrendPointDto;
import com.hrmanagement.entity.Attendance;
import com.hrmanagement.entity.Employee;
import com.hrmanagement.entity.LeaveBalance;
import com.hrmanagement.entity.User;
import com.hrmanagement.repository.AttendanceRepository;
import com.hrmanagement.repository.DashboardRepository;
import com.hrmanagement.repository.EmployeeRepository;
import com.hrmanagement.repository.LeaveBalanceRepository;
import com.hrmanagement.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserRepository userRepository;

    public DashboardService(DashboardRepository dashboardRepository,
                            EmployeeRepository employeeRepository,
                            AttendanceRepository attendanceRepository,
                            LeaveBalanceRepository leaveBalanceRepository,
                            UserRepository userRepository) {
        this.dashboardRepository = dashboardRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.userRepository = userRepository;
    }

    public DashboardStatsDto getDashboardStats() {
        LocalDate today = LocalDate.now();

        long totalEmployees = dashboardRepository.countActiveEmployees();
        long presentOrLateCount = dashboardRepository.countTodayPresentOrLate(today);
        long employeesOnLeaveToday = dashboardRepository.countDistinctEmployeesOnLeaveToday(today);

        double attendanceRatePercent = 0.0;
        if (totalEmployees > 0) {
            attendanceRatePercent = Math.round((double) presentOrLateCount / totalEmployees * 1000.0) / 10.0;
        }

        return new DashboardStatsDto(totalEmployees, attendanceRatePercent, employeesOnLeaveToday);
    }

    public EmployeeDashboardStatsDto getEmployeeDashboardStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй"));

        Employee employee = employeeRepository.findAll().stream()
                .filter(e -> e.getUser() != null && e.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ажилтан олдсонгүй"));

        Long empId = employee.getId();
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();
        int currentYear = currentYear = currentMonth.getYear();

        long totalEmployees = dashboardRepository.countActiveEmployees();

        List<Attendance> monthlyAttendance = attendanceRepository.findMonthlyByEmployee(empId, monthStart, monthEnd);
        int totalDays = monthlyAttendance.size();
        int presentOrLateDays = 0;
        for (int i = 0; i < monthlyAttendance.size(); i++) {
            Attendance a = monthlyAttendance.get(i);
            String status = a.getStatus().name();
            if ("PRESENT".equals(status) || "LATE".equals(status)) {
                presentOrLateDays++;
            }
        }

        double attendanceRate = 0.0;
        if (totalDays > 0) {
            attendanceRate = Math.round((double) presentOrLateDays / totalDays * 1000.0) / 10.0;
        }

        int usedLeaveDays = dashboardRepository.sumEmployeeUsedLeaveDays(empId, currentYear);
        int remainingLeaveDays = dashboardRepository.sumEmployeeRemainingLeaveDays(empId, currentYear);

        return new EmployeeDashboardStatsDto(totalEmployees, attendanceRate, usedLeaveDays, remainingLeaveDays);
    }

    public List<SalaryTrendPointDto> getLastSixMonthsSalaryTrend() {
        YearMonth currentMonth = YearMonth.now();
        List<SalaryTrendPointDto> trend = new ArrayList<>();

        BigDecimal totalSalary = BigDecimal.ZERO;
        List<Employee> allEmployees = employeeRepository.findAll();
        for (int i = 0; i < allEmployees.size(); i++) {
            Employee e = allEmployees.get(i);
            if (!"TERMINATED".equals(e.getStatus().name())) {
                totalSalary = totalSalary.add(e.getSalary() != null ? e.getSalary() : BigDecimal.ZERO);
            }
        }

        YearMonth ym = currentMonth.minusMonths(5);
        for (int i = 0; i < 6; i++) {
            String monthLabel = ym.getMonthValue() + " сар";
            trend.add(new SalaryTrendPointDto(monthLabel, totalSalary.doubleValue()));
            ym = ym.plusMonths(1);
        }

        return trend;
    }
}
