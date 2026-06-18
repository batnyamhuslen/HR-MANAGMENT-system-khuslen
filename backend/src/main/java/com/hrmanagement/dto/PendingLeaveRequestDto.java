package com.hrmanagement.dto;

public class PendingLeaveRequestDto {

    private Long id;
    private String employeeName;
    private String initials;
    private String leaveTypeName;
    private int totalDays;

    public PendingLeaveRequestDto() {}

    public PendingLeaveRequestDto(Long id, String employeeName, String initials, String leaveTypeName, int totalDays) {
        this.id = id;
        this.employeeName = employeeName;
        this.initials = initials;
        this.leaveTypeName = leaveTypeName;
        this.totalDays = totalDays;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getInitials() { return initials; }
    public void setInitials(String initials) { this.initials = initials; }
    public String getLeaveTypeName() { return leaveTypeName; }
    public void setLeaveTypeName(String leaveTypeName) { this.leaveTypeName = leaveTypeName; }
    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }
}
