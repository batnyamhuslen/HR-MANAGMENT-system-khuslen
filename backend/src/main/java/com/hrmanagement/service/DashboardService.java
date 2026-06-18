package com.hrmanagement.service;

import com.hrmanagement.dto.DashboardStatsDto;
import com.hrmanagement.dto.SalaryTrendPointDto;
import com.hrmanagement.entity.Employee;
import com.hrmanagement.repository.AttendanceRepository;
import com.hrmanagement.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveService leaveService;

    public DashboardService(EmployeeRepository employeeRepository,
                            AttendanceRepository attendanceRepository,
                            LeaveService leaveService) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveService = leaveService;
    }

    public DashboardStatsDto getDashboardStats() {
        long totalEmployees = employeeRepository.countByStatusNot("TERMINATED");

        LocalDate today = LocalDate.now();
        long presentCount = attendanceRepository.countByDateAndStatusIn(today,
                List.of("PRESENT", "LATE"));

        long activeEmployees = employeeRepository.countByStatusNot("TERMINATED");
        double attendanceRate = 0;
        if (activeEmployees > 0) {
            attendanceRate = Math.round((double) presentCount / activeEmployees * 1000.0) / 10.0;
        }

        long employeesOnLeaveToday = leaveService.getEmployeesOnLeaveToday();

        return new DashboardStatsDto(totalEmployees, attendanceRate, employeesOnLeaveToday);
    }

    public List<SalaryTrendPointDto> getLastSixMonthsSalaryTrend() {
        YearMonth currentMonth = YearMonth.now();
        List<SalaryTrendPointDto> trend = new ArrayList<>();

        BigDecimal totalSalary = employeeRepository.findAll().stream()
                .filter(e -> !"TERMINATED".equals(e.getStatus().name()))
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = currentMonth.minusMonths(i);
            String monthLabel = ym.getMonthValue() + " сар";
            trend.add(new SalaryTrendPointDto(monthLabel, totalSalary.doubleValue()));
        }

        return trend;
    }
}
