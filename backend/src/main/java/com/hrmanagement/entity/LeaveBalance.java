package com.hrmanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_balances", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "leave_type_id", "year"})
})
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private int year;

    @Column(name = "total_allocated", nullable = false)
    private int totalAllocated = 0;

    @Column(nullable = false)
    private int used = 0;

    @Column(name = "remaining", insertable = false, updatable = false)
    private int remaining;

    public LeaveBalance() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getTotalAllocated() { return totalAllocated; }
    public void setTotalAllocated(int totalAllocated) { this.totalAllocated = totalAllocated; }
    public int getUsed() { return used; }
    public void setUsed(int used) { this.used = used; }
    public int getRemaining() { return remaining; }
    public void setRemaining(int remaining) { this.remaining = remaining; }
}
