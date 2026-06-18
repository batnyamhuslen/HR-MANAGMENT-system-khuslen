CREATE TABLE leave_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    default_days_per_year INT NOT NULL DEFAULT 10
);
