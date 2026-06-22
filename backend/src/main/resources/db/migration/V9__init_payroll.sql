CREATE TYPE payroll_status AS ENUM ('DRAFT', 'FINALIZED', 'PAID');

CREATE TABLE payroll_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(50) NOT NULL UNIQUE,
    config_value NUMERIC(14, 4) NOT NULL,
    description VARCHAR(255),
    effective_from DATE NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO payroll_config (config_key, config_value, description, effective_from) VALUES
    ('SOCIAL_INSURANCE_EMPLOYEE_RATE', 0.135, 'Employee-side social insurance rate (НДШ)', '2026-01-01'),
    ('SOCIAL_INSURANCE_EMPLOYER_RATE', 0.125, 'Employer-side social insurance rate (НДШ, standard risk class)', '2026-01-01'),
    ('SOCIAL_INSURANCE_CAP', 7920000, 'Max monthly income subject to social insurance (10x minimum wage)', '2026-01-01'),
    ('INCOME_TAX_RATE', 0.10, 'Flat personal income tax rate (ХХОАТ)', '2026-01-01'),
    ('OVERTIME_MULTIPLIER', 1.5, 'Overtime pay multiplier over standard hourly rate', '2026-01-01'),
    ('MINIMUM_WAGE', 792000, 'Current statutory minimum monthly wage', '2026-01-01')
ON CONFLICT (config_key) DO NOTHING;

CREATE TABLE payroll_records (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    pay_period_year INTEGER NOT NULL,
    pay_period_month INTEGER NOT NULL CHECK (pay_period_month BETWEEN 1 AND 12),
    base_salary NUMERIC(14, 2) NOT NULL,
    overtime_hours NUMERIC(6, 2) NOT NULL DEFAULT 0,
    overtime_pay NUMERIC(14, 2) NOT NULL DEFAULT 0,
    allowances NUMERIC(14, 2) NOT NULL DEFAULT 0,
    unpaid_leave_deduction NUMERIC(14, 2) NOT NULL DEFAULT 0,
    gross_salary NUMERIC(14, 2) NOT NULL,
    social_insurance_employee NUMERIC(14, 2) NOT NULL,
    social_insurance_employer NUMERIC(14, 2) NOT NULL,
    taxable_income NUMERIC(14, 2) NOT NULL,
    income_tax NUMERIC(14, 2) NOT NULL,
    other_deductions NUMERIC(14, 2) NOT NULL DEFAULT 0,
    net_salary NUMERIC(14, 2) NOT NULL,
    status payroll_status NOT NULL DEFAULT 'DRAFT',
    generated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    finalized_at TIMESTAMP,
    CONSTRAINT uq_payroll_employee_period UNIQUE (employee_id, pay_period_year, pay_period_month)
);

CREATE INDEX idx_payroll_employee_id ON payroll_records(employee_id);
CREATE INDEX idx_payroll_period ON payroll_records(pay_period_year, pay_period_month);
CREATE INDEX idx_payroll_status ON payroll_records(status);

CREATE CAST (VARCHAR AS payroll_status) WITH INOUT AS IMPLICIT;
