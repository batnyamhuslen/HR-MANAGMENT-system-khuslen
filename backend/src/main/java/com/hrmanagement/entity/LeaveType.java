package com.hrmanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_types")
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "default_days_per_year", nullable = false)
    private int defaultDaysPerYear = 10;

    public LeaveType() {}

    public LeaveType(String name, int defaultDaysPerYear) {
        this.name = name;
        this.defaultDaysPerYear = defaultDaysPerYear;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getDefaultDaysPerYear() { return defaultDaysPerYear; }
    public void setDefaultDaysPerYear(int defaultDaysPerYear) { this.defaultDaysPerYear = defaultDaysPerYear; }
}
