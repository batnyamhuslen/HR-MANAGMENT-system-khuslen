CREATE TYPE attendance_status AS ENUM ('PRESENT', 'ABSENT', 'LATE', 'HALF_DAY');

CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    date DATE NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    status attendance_status NOT NULL DEFAULT 'PRESENT',
    total_hours DECIMAL(4,1),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(employee_id, date)
);

CREATE INDEX idx_attendance_employee ON attendance(employee_id);
CREATE INDEX idx_attendance_date ON attendance(date);
CREATE INDEX idx_attendance_status ON attendance(status);

CREATE OR REPLACE FUNCTION compute_total_hours()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.check_in_time IS NOT NULL AND NEW.check_out_time IS NOT NULL THEN
        NEW.total_hours := ROUND(
            EXTRACT(EPOCH FROM (NEW.check_out_time - NEW.check_in_time)) / 3600.0,
            1
        );
    ELSE
        NEW.total_hours := NULL;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_compute_total_hours
    BEFORE INSERT OR UPDATE ON attendance
    FOR EACH ROW
    EXECUTE FUNCTION compute_total_hours();
