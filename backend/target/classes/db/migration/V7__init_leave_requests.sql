CREATE TYPE leave_request_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED');

CREATE TABLE leave_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    leave_type_id BIGINT NOT NULL REFERENCES leave_types(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INT NOT NULL,
    reason TEXT,
    status leave_request_status NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT REFERENCES users(id),
    approved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_leave_requests_employee ON leave_requests(employee_id);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);
CREATE INDEX idx_leave_requests_dates ON leave_requests(start_date, end_date);

CREATE TABLE leave_balances (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    leave_type_id BIGINT NOT NULL REFERENCES leave_types(id),
    year INT NOT NULL,
    total_allocated INT NOT NULL DEFAULT 0,
    used INT NOT NULL DEFAULT 0,
    remaining INT GENERATED ALWAYS AS (total_allocated - used) STORED,
    UNIQUE(employee_id, leave_type_id, year)
);

CREATE INDEX idx_leave_balances_employee ON leave_balances(employee_id);
