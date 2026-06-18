# HR Management System — Implementation Plan

**Approved by user on 2026-06-18.** Ready to execute.

---

## Environment

| Tool | Version | Notes |
|------|---------|-------|
| Java | 21 (Temurin) | Spec says 17, fully compatible |
| Maven | 3.9.12 | |
| Node | 24.13.1 | |
| Angular CLI | Latest | |
| PostgreSQL | 14.18 | Running on localhost:5432 |

## Defaults

| Parameter | Value |
|-----------|-------|
| DB name | `HR_Khuslen` |
| DB user | `postgres` |
| DB password | `postgres` |
| DB host | `localhost:5433` |
| Backend port | `8080` |
| Frontend port | `4200` |
| Java target | 17 (to match spec) |

---

## Execution Steps

### Phase 1: Backend Scaffold

**Files to create:**

1. `backend/pom.xml` — Spring Boot 3.3.0 parent, dependencies:
   - spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, spring-boot-starter-validation
   - flyway-core, flyway-database-postgresql
   - postgresql (runtime scope)
   - jjwt-api 0.12.5, jjwt-impl, jjwt-jackson
   - spring-boot-starter-test, spring-security-test

2. `backend/src/main/java/com/hrmanagement/HrManagementApplication.java` — `@SpringBootApplication` main class

3. `backend/src/main/resources/application.yml` — dev profile with:
   - PostgreSQL: `jdbc:postgresql://localhost:5432/hr_management`, user `postgres`, pass `postgres`
   - JPA: `ddl-auto: validate`, show-sql: true
   - Flyway: enabled, locations: `classpath:db/migration`
   - JWT: secret (random base64 string), expiration 86400000ms (24h)

4. `backend/src/main/resources/application-prod.yml` — prod profile (stub for later)

**Verify:** `mvn compile` succeeds.

---

### Phase 2: Database — Flyway Migrations

**V1__init_users.sql:**
```sql
CREATE TYPE user_role AS ENUM ('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE');

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role user_role NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
```

**HashGenerator.java (one-time):**
- Written at `backend/src/main/java/com/hrmanagement/HashGenerator.java`
- Uses `BCryptPasswordEncoder` from Spring Security Crypto
- Prints hashes for: `admin123`, `hr123`, `employee123`
- Run via: `cd backend && mvn compile exec:java -Dexec.mainClass="com.hrmanagement.HashGenerator"`
- Capture 3 hashes

**V2__seed_users.sql:**
```sql
INSERT INTO users (username, password, email, role) VALUES
('admin', '<hash_admin>', 'admin@hr.mn', 'ADMIN'),
('hr', '<hash_hr>', 'hr@hr.mn', 'HR'),
('employee', '<hash_employee>', 'employee@hr.mn', 'EMPLOYEE');
```

**Delete HashGenerator.java** after capturing hashes.

**V3__init_departments.sql:**
```sql
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);
```

**V4__init_employees.sql:**
```sql
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
```

**V5__init_attendance.sql:**
```sql
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

-- Trigger to auto-compute total_hours
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
```

**V6__init_leave_types.sql:**
```sql
CREATE TABLE leave_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    default_days_per_year INT NOT NULL DEFAULT 10
);
```

**V7__init_leave_requests.sql:**
```sql
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
```

**Verify:** `mvn flyway:migrate -Dflyway.url=...` runs all migrations cleanly.

---

### Phase 3: Auth Module

**Files to create (in order):**

1. `enums/UserRole.java` — enum ADMIN, HR, MANAGER, EMPLOYEE
2. `entity/User.java` — matches users table, explicit getters/setters/constructors, no Lombok
3. `repository/UserRepository.java` — `findByUsername`, `existsByUsername`, `existsByEmail`
4. `security/JwtUtil.java` — generate/validate/extract claims from JWT
5. `security/CustomUserDetailsService.java` — implements UserDetailsService, loads by username
6. `security/JwtAuthenticationFilter.java` — extends OncePerRequestFilter, extracts JWT from Authorization header, validates, sets SecurityContext
7. `config/SecurityConfig.java`:
   - Stateless session management
   - JWT filter chain
   - CORS: allow `http://localhost:4200`
   - Permit `/api/auth/login`, `/api/auth/register`; all else authenticated
   - Role-based: ADMIN for register, ADMIN/HR for employee management, etc.
8. `dto/AuthRequest.java`, `dto/AuthResponse.java`, `dto/RegisterRequest.java`, `dto/UserProfileDto.java`
9. `exception/GlobalExceptionHandler.java` — `@ControllerAdvice` returning `{timestamp, status, message, path}`
10. `exception/ErrorResponse.java` — DTO for error responses
11. `controller/AuthController.java`:
    - `POST /api/auth/login` → returns JWT + user profile
    - `POST /api/auth/register` → ADMIN only, creates user
    - `GET /api/auth/me` → returns `{fullName, initials, roleLabel}` with Mongolian labels

**Mongolian role labels:**
- ADMIN → "Админ"
- HR → "HR Менежер"
- MANAGER → "Менежер"
- EMPLOYEE → "Ажилтан"

**Verify:** Start app, login with all 3 users via curl, confirm valid JWT returned, `/api/auth/me` returns correct labels.

---

### Phase 4: Employee & Department CRUD

**Files to create:**

1. `entity/Department.java`
2. `entity/Employee.java` + `enums/EmployeeStatus.java`
3. `repository/DepartmentRepository.java`
4. `repository/EmployeeRepository.java` — with search/filter queries:
   - `@Query` for search by name, department, status with pagination
5. `dto/DepartmentDto.java`, `dto/EmployeeDto.java`, `dto/EmployeeCreateRequest.java`, `dto/EmployeeUpdateRequest.java`
6. `service/DepartmentService.java`, `service/EmployeeService.java`
7. `controller/DepartmentController.java` — CRUD
8. `controller/EmployeeController.java` — CRUD with:
   - `GET /api/employees?page=0&size=10&search=&departmentId=&status=`
   - `GET /api/employees/{id}`
   - `POST /api/employees`
   - `PUT /api/employees/{id}`
   - `DELETE /api/employees/{id}`

**Verify:** CRUD operations via curl, paginated search works.

---

### Phase 5: Attendance

**Files to create:**

1. `enums/AttendanceStatus.java`
2. `entity/Attendance.java`
3. `repository/AttendanceRepository.java` — date range queries, monthly summary
4. `service/AttendanceService.java` — check-in, check-out logic
5. `controller/AttendanceController.java`:
   - `POST /api/attendance/check-in`
   - `POST /api/attendance/check-out`
   - `GET /api/attendance?employeeId=&from=&to=`
   - `GET /api/attendance/monthly?employeeId=&year=&month=`
   - `GET /api/attendance/daily` (admin view)

**Verify:** Check-in/check-out flow works, `total_hours` auto-computes in DB.

---

### Phase 6: Leave Management

**Files to create:**

1. `entity/LeaveType.java`
2. `entity/LeaveRequest.java` + `enums/LeaveRequestStatus.java`
3. `entity/LeaveBalance.java`
4. `repository/LeaveTypeRepository.java`
5. `repository/LeaveRequestRepository.java`
6. `repository/LeaveBalanceRepository.java`
7. `service/LeaveService.java` — approval workflow, balance deduction/restoration, overlap prevention
8. `controller/LeaveController.java`:
   - `POST /api/leave-requests` — submit
   - `GET /api/leave-requests/pending?limit=N` — pending requests (default 10)
   - `POST /api/leave-requests/{id}/approve` — HR/ADMIN/MANAGER
   - `POST /api/leave-requests/{id}/reject` — HR/ADMIN/MANAGER
   - `POST /api/leave-requests/{id}/cancel` — employee cancels own
   - `GET /api/leave-requests/my` — current user's requests
   - `GET /api/leave-requests/calendar?from=&to=`
   - `GET /api/leave-balances` — current user's balances

**Verify:** Submit → approve → balance deducted; cancel approved → balance restored.

---

### Phase 7: Dashboard Backend

**Files to create:**

1. `dto/DashboardStatsDto.java`
2. `dto/SalaryTrendPointDto.java`
3. `dto/PendingLeaveRequestDto.java` (for leave requests query)
4. `service/DashboardService.java`:
   - `getDashboardStats()` — `@Query` for counts
   - `getLastSixMonthsSalaryTrend()` — group by month via JPQL or native
5. `controller/DashboardController.java`:
   - `GET /api/dashboard/stats` — ADMIN/HR/MANAGER
   - `GET /api/dashboard/salary-trend` — ADMIN/HR

**Verify:** Both endpoints return realistic data from seeded records.

---

### Phase 8: Frontend Scaffold

**Commands:**
```bash
cd frontend && ng new hr-management-frontend --standalone --routing --style=scss --prefix=app
```

Then install dependencies:
```bash
npm install @angular/material @angular/cdk
```

Add Tabler Icons via CDN in `index.html`:
```html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@tabler/icons-webfont@latest/dist/tabler-icons.min.css" />
```

**Files to create:**

1. `app/app.config.ts` — `provideRouter(routes)`, `provideHttpClient(withFetch())`, `provideAnimations()`
2. `app/app.routes.ts` — lazy-loaded routes:
   - `{ path: 'auth', loadComponent: () => ... }`
   - `{ path: 'dashboard', loadComponent: () => ..., canActivate: [authGuard] }`
   - `{ path: 'employees', loadChildren: () => ..., canActivate: [authGuard] }`
   - `{ path: 'attendance', ... }`
   - `{ path: 'leave', ... }`
   - `{ path: '', redirectTo: '/dashboard', pathMatch: 'full' }`
3. `app/guards/auth.guard.ts` — `CanActivateFn`, checks token in localStorage
4. `app/guards/role.guard.ts` — `CanActivateFn`, checks user role
5. `app/interceptors/auth.interceptor.ts` — SSR-safe, attaches JWT to requests
6. `app/services/auth.service.ts` — login, logout, `getCurrentUser()`, `isLoggedIn()`
7. `app/services/dashboard.service.ts`, `employee.service.ts`, `attendance.service.ts`, `leave.service.ts`
8. `app/shared/` — data-table, confirm-dialog, toast, loading-spinner, skeleton components

**Verify:** `ng serve` starts, app loads without errors.

---

### Phase 9: Frontend Dashboard

**Components to create:**

1. `app/features/auth/login/login.component.ts` — standalone, reactive form, calls auth service
2. `app/features/dashboard/dashboard.component.ts` — signals for state:
   - `stats`, `salaryTrend`, `pendingLeaves`, `currentUser`
   - Loading flags: `statsLoading`, `chartLoading`, `leavesLoading`
   - `toggleSidebar()`, `closeSidebar()`
   - `approveLeave(id)` — calls API, removes optimistically
   - `barHeightPercent(point)` — scales bar height relative to max
   - On init: fetch stats, salary trend, pending leaves, current user

3. `dashboard.component.html`:
   - Dark sidebar (`#0f172a`), logo "HR CORE", nav links with Tabler icons + routerLinkActive
   - Mobile backdrop overlay
   - Topbar: hamburger (mobile only), search, notification bell with red dot
   - Main content:
     - Heading "Тавтай морил 👋" + subtitle "Өнөөдрийн байдлаар..."
     - 3 stat cards: Нийт ажилтан / Өнөөдрийн ирц / Чөлөөтэй байгаа
       - Icons: `users`, `calendar-check`, `calendar-off`
       - Colors: blue, amber, rose for icon backgrounds
     - 6-month salary bar chart — bars scaled via `barHeightPercent()`
     - Pending leave requests card (up to 3 items, avatar/initials, approve button)

4. `dashboard.component.scss`:
   - Sidebar: `#0f172a` bg, `#020617` header/footer strip, fixed left 280px
   - White cards: `#ffffff`, 1px `#e5e7eb` border, `box-shadow: 0 1px 3px rgba(0,0,0,0.1)`, `border-radius: 0.75rem`
   - Primary blue: `#2563eb`
   - Skeleton shimmer animation
   - Mobile: sidebar `position: fixed; left: -100%; transition: left 0.3s`, opens to `left: 0`
   - Stat grid: `grid-template-columns: repeat(3, 1fr)` → `1fr` at mobile
   - Content grid collapses to single column at 768px

**Verify:** Login → lands on `/dashboard` → real stats render → chart renders → approve removes item → resize to mobile → hamburger menu works.

---

### Phase 10: Remaining Pages + README

**Pages:**
- `EmployeeListComponent` — table with search/pagination/filter
- `EmployeeDetailComponent` — form for create/edit
- `AttendanceListComponent` — table with date range filter, check-in/check-out button
- `LeaveRequestListComponent` — table with pending count badge, approve/reject buttons
- `LeaveRequestFormComponent` — submit leave request

All use the shared sidebar/topbar shell, same card/color tokens as dashboard.

**README.md:**
```markdown
# HR Management System

## Prerequisites
- Java 17+
- Maven 3.8+
- Node 18+
- PostgreSQL 14+

## Database Setup
```bash
createdb hr_management
```

## Backend
```bash
cd backend
mvn spring-boot:run
```

## Frontend
```bash
cd frontend
npm install
ng serve
```

## Default Credentials
| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| hr | hr123 | HR |
| employee | employee123 | EMPLOYEE |

API Base URL: http://localhost:8080
```

**Final Verify:** `mvn clean package -DskipTests` + `ng build` both succeed.

---

## Key Design Decisions

1. **No Lombok** — all entities/DTOs have explicit constructors, getters, setters
2. **Native PostgreSQL ENUMs** — mapped via `@Enumerated(EnumType.STRING)` + `columnDefinition`
3. **Flyway-only schema** — `ddl-auto=validate` never allows Hibernate auto-DDL
4. **SSR-safe frontend** — all `localStorage` access guarded by `isPlatformBrowser`
5. **Angular signals** — used for reactive state in dashboard
6. **Tabler Icons** — via CDN webfont for fastest setup
