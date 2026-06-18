package com.hrmanagement.dto;

public class EmployeeDashboardStatsDto {

    private long totalEmployees;
    private double attendanceRateThisMonth;
    private int usedLeaveDays;
    private int remainingLeaveDays;

    public EmployeeDashboardStatsDto() {}

    public EmployeeDashboardStatsDto(long totalEmployees, double attendanceRateThisMonth, int usedLeaveDays, int remainingLeaveDays) {
        this.totalEmployees = totalEmployees;
        this.attendanceRateThisMonth = attendanceRateThisMonth;
        this.usedLeaveDays = usedLeaveDays;
        this.remainingLeaveDays = remainingLeaveDays;
    }

    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }
    public double getAttendanceRateThisMonth() { return attendanceRateThisMonth; }
    public void setAttendanceRateThisMonth(double attendanceRateThisMonth) { this.attendanceRateThisMonth = attendanceRateThisMonth; }
    public int getUsedLeaveDays() { return usedLeaveDays; }
    public void setUsedLeaveDays(int usedLeaveDays) { this.usedLeaveDays = usedLeaveDays; }
    public int getRemainingLeaveDays() { return remainingLeaveDays; }
    public void setRemainingLeaveDays(int remainingLeaveDays) { this.remainingLeaveDays = remainingLeaveDays; }
}
