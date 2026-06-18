package com.hrmanagement.dto;

public class DashboardStatsDto {

    private long totalEmployees;
    private double attendanceRatePercent;
    private long employeesOnLeaveToday;

    public DashboardStatsDto() {}

    public DashboardStatsDto(long totalEmployees, double attendanceRatePercent, long employeesOnLeaveToday) {
        this.totalEmployees = totalEmployees;
        this.attendanceRatePercent = attendanceRatePercent;
        this.employeesOnLeaveToday = employeesOnLeaveToday;
    }

    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }
    public double getAttendanceRatePercent() { return attendanceRatePercent; }
    public void setAttendanceRatePercent(double attendanceRatePercent) { this.attendanceRatePercent = attendanceRatePercent; }
    public long getEmployeesOnLeaveToday() { return employeesOnLeaveToday; }
    public void setEmployeesOnLeaveToday(long employeesOnLeaveToday) { this.employeesOnLeaveToday = employeesOnLeaveToday; }
}
