package com.hrmanagement.dto.payroll;

import java.math.BigDecimal;

public class PayrollRecordDto {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private int payPeriodYear;
    private int payPeriodMonth;
    private BigDecimal baseSalary;
    private BigDecimal overtimePay;
    private BigDecimal allowances;
    private BigDecimal unpaidLeaveDeduction;
    private BigDecimal grossSalary;
    private BigDecimal socialInsuranceEmployee;
    private BigDecimal incomeTax;
    private BigDecimal otherDeductions;
    private BigDecimal netSalary;
    private String status;

    public PayrollRecordDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public int getPayPeriodYear() { return payPeriodYear; }
    public void setPayPeriodYear(int payPeriodYear) { this.payPeriodYear = payPeriodYear; }
    public int getPayPeriodMonth() { return payPeriodMonth; }
    public void setPayPeriodMonth(int payPeriodMonth) { this.payPeriodMonth = payPeriodMonth; }
    public BigDecimal getBaseSalary() { return baseSalary; }
    public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }
    public BigDecimal getOvertimePay() { return overtimePay; }
    public void setOvertimePay(BigDecimal overtimePay) { this.overtimePay = overtimePay; }
    public BigDecimal getAllowances() { return allowances; }
    public void setAllowances(BigDecimal allowances) { this.allowances = allowances; }
    public BigDecimal getUnpaidLeaveDeduction() { return unpaidLeaveDeduction; }
    public void setUnpaidLeaveDeduction(BigDecimal unpaidLeaveDeduction) { this.unpaidLeaveDeduction = unpaidLeaveDeduction; }
    public BigDecimal getGrossSalary() { return grossSalary; }
    public void setGrossSalary(BigDecimal grossSalary) { this.grossSalary = grossSalary; }
    public BigDecimal getSocialInsuranceEmployee() { return socialInsuranceEmployee; }
    public void setSocialInsuranceEmployee(BigDecimal socialInsuranceEmployee) { this.socialInsuranceEmployee = socialInsuranceEmployee; }
    public BigDecimal getIncomeTax() { return incomeTax; }
    public void setIncomeTax(BigDecimal incomeTax) { this.incomeTax = incomeTax; }
    public BigDecimal getOtherDeductions() { return otherDeductions; }
    public void setOtherDeductions(BigDecimal otherDeductions) { this.otherDeductions = otherDeductions; }
    public BigDecimal getNetSalary() { return netSalary; }
    public void setNetSalary(BigDecimal netSalary) { this.netSalary = netSalary; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
