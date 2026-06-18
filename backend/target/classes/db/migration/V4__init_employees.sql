CREATE TYPE employee_status AS ENUM ('ACTIVE', 'ON_LEAVE', 'TERMINATED');

CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    employee_code VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(30),
    date_of_birth DATE,
    hire_date DATE NOT NULL,
    department_id BIGINT REFERENCES departments(id),
    position VARCHAR(100),
    salary DECIMAL(12,2) DEFAULT 0,
    status employee_status NOT NULL DEFAULT 'ACTIVE',
    manager_id BIGINT REFERENCES employees(id),
    user_id BIGINT UNIQUE REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_employees_department ON employees(department_id);
CREATE INDEX idx_employees_status ON employees(status);
CREATE INDEX idx_employees_manager ON employees(manager_id);
