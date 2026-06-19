package com.hrmanagement.dto;

public class DashboardStatsDto {

    private long totalEmployees;
    private Double attendanceRatePercent;
    private Long employeesOnLeaveToday;

    public DashboardStatsDto() {}

    public DashboardStatsDto(long totalEmployees, Double attendanceRatePercent, Long employeesOnLeaveToday) {
        this.totalEmployees = totalEmployees;
        this.attendanceRatePercent = attendanceRatePercent;
        this.employeesOnLeaveToday = employeesOnLeaveToday;
    }

    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }
    public Double getAttendanceRatePercent() { return attendanceRatePercent; }
    public void setAttendanceRatePercent(Double attendanceRatePercent) { this.attendanceRatePercent = attendanceRatePercent; }
    public Long getEmployeesOnLeaveToday() { return employeesOnLeaveToday; }
    public void setEmployeesOnLeaveToday(Long employeesOnLeaveToday) { this.employeesOnLeaveToday = employeesOnLeaveToday; }
}
