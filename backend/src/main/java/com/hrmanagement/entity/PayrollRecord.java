package com.hrmanagement.entity;

import com.hrmanagement.enums.PayrollStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_records", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "pay_period_year", "pay_period_month"})
})
public class PayrollRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "pay_period_year", nullable = false)
    private int payPeriodYear;

    @Column(name = "pay_period_month", nullable = false)
    private int payPeriodMonth;

    @Column(name = "base_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "overtime_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "overtime_pay", nullable = false, precision = 14, scale = 2)
    private BigDecimal overtimePay = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal allowances = BigDecimal.ZERO;

    @Column(name = "unpaid_leave_deduction", nullable = false, precision = 14, scale = 2)
    private BigDecimal unpaidLeaveDeduction = BigDecimal.ZERO;

    @Column(name = "gross_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal grossSalary;

    @Column(name = "social_insurance_employee", nullable = false, precision = 14, scale = 2)
    private BigDecimal socialInsuranceEmployee;

    @Column(name = "social_insurance_employer", nullable = false, precision = 14, scale = 2)
    private BigDecimal socialInsuranceEmployer;

    @Column(name = "taxable_income", nullable = false, precision = 14, scale = 2)
    private BigDecimal taxableIncome;

    @Column(name = "income_tax", nullable = false, precision = 14, scale = 2)
    private BigDecimal incomeTax;

    @Column(name = "other_deductions", nullable = false, precision = 14, scale = 2)
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    @Column(name = "net_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "payroll_status")
    private PayrollStatus status = PayrollStatus.DRAFT;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }

    public PayrollRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public int getPayPeriodYear() { return payPeriodYear; }
    public void setPayPeriodYear(int payPeriodYear) { this.payPeriodYear = payPeriodYear; }
    public int getPayPeriodMonth() { return payPeriodMonth; }
    public void setPayPeriodMonth(int payPeriodMonth) { this.payPeriodMonth = payPeriodMonth; }
    public BigDecimal getBaseSalary() { return baseSalary; }
    public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }
    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
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
    public BigDecimal getSocialInsuranceEmployer() { return socialInsuranceEmployer; }
    public void setSocialInsuranceEmployer(BigDecimal socialInsuranceEmployer) { this.socialInsuranceEmployer = socialInsuranceEmployer; }
    public BigDecimal getTaxableIncome() { return taxableIncome; }
    public void setTaxableIncome(BigDecimal taxableIncome) { this.taxableIncome = taxableIncome; }
    public BigDecimal getIncomeTax() { return incomeTax; }
    public void setIncomeTax(BigDecimal incomeTax) { this.incomeTax = incomeTax; }
    public BigDecimal getOtherDeductions() { return otherDeductions; }
    public void setOtherDeductions(BigDecimal otherDeductions) { this.otherDeductions = otherDeductions; }
    public BigDecimal getNetSalary() { return netSalary; }
    public void setNetSalary(BigDecimal netSalary) { this.netSalary = netSalary; }
    public PayrollStatus getStatus() { return status; }
    public void setStatus(PayrollStatus status) { this.status = status; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public LocalDateTime getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(LocalDateTime finalizedAt) { this.finalizedAt = finalizedAt; }
}
