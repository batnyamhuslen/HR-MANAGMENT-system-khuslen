package com.hrmanagement.dto;

public class LeaveBalanceDto {

    private Long id;
    private Long employeeId;
    private Long leaveTypeId;
    private String leaveTypeName;
    private int year;
    private int totalAllocated;
    private int used;
    private int remaining;

    public LeaveBalanceDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Long getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(Long leaveTypeId) { this.leaveTypeId = leaveTypeId; }
    public String getLeaveTypeName() { return leaveTypeName; }
    public void setLeaveTypeName(String leaveTypeName) { this.leaveTypeName = leaveTypeName; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getTotalAllocated() { return totalAllocated; }
    public void setTotalAllocated(int totalAllocated) { this.totalAllocated = totalAllocated; }
    public int getUsed() { return used; }
    public void setUsed(int used) { this.used = used; }
    public int getRemaining() { return remaining; }
    public void setRemaining(int remaining) { this.remaining = remaining; }
}
