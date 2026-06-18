# HR Management System

Full-stack HR management system built with Spring Boot 3.x + Angular 17+.

## Tech Stack

- **Backend:** Spring Boot 3.3.0, Java 17, Spring Security, Spring Data JPA, JWT, Flyway, PostgreSQL
- **Frontend:** Angular 17, Angular Material, Tabler Icons, SCSS
- **Database:** PostgreSQL 14+

## Prerequisites

- Java 17+ (built with JDK 21)
- Maven 3.8+
- Node.js 18+
- PostgreSQL 14+

## Database Setup

```bash
# Connect to PostgreSQL and create the database
psql -U postgres -h 127.0.0.1
CREATE DATABASE hr_management;
\q
```

Or use the command line:
```bash
createdb -U postgres -h 127.0.0.1 hr_management
```

The default connection is `jdbc:postgresql://127.0.0.1:5432/hr_management` with user `postgres` and password `postgres`. Configure in `backend/src/main/resources/application.yml`.

## Running the Backend

```bash
cd backend
mvn spring-boot:run
```

The API starts at `http://localhost:8080`. Flyway runs migrations automatically on startup.

## Running the Frontend

```bash
cd frontend
npm install
ng serve
```

The UI starts at `http://localhost:4200`.

## Default Credentials

| Username   | Password      | Role     |
|------------|---------------|----------|
| admin      | admin123      | ADMIN    |
| hr         | hr123         | HR       |
| employee   | employee123   | EMPLOYEE |

## API Base URL

`http://localhost:8080`

### Key Endpoints

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/auth/login` | POST | - | Login, returns JWT |
| `/api/auth/register` | POST | ADMIN | Create new user |
| `/api/auth/me` | GET | Any | Current user profile |
| `/api/dashboard/stats` | GET | ADMIN/HR/MANAGER | Dashboard statistics |
| `/api/dashboard/salary-trend` | GET | ADMIN/HR | 6-month salary trend |
| `/api/employees` | GET/POST | Any (GET), ADMIN/HR (POST) | Employee CRUD |
| `/api/employees/{id}` | GET/PUT/DELETE | Varies | Single employee |
| `/api/departments` | GET/POST | Any (GET), ADMIN/HR (POST) | Department CRUD |
| `/api/attendance/check-in` | POST | Any | Employee check-in |
| `/api/attendance/check-out` | POST | Any | Employee check-out |
| `/api/attendance` | GET | Any | Attendance by date range |
| `/api/attendance/daily` | GET | ADMIN/HR | Daily attendance view |
| `/api/leave-requests` | POST | Any | Submit leave request |
| `/api/leave-requests/pending` | GET | ADMIN/HR/MANAGER | Pending approvals |
| `/api/leave-requests/{id}/approve` | POST | ADMIN/HR/MANAGER | Approve leave |
| `/api/leave-requests/{id}/reject` | POST | ADMIN/HR/MANAGER | Reject leave |

## Project Structure

```
├── backend/
│   ├── src/main/java/com/hrmanagement/
│   │   ├── config/         # Security, CORS config
│   │   ├── controller/     # REST controllers
│   │   ├── dto/            # Request/response DTOs
│   │   ├── entity/         # JPA entities
│   │   ├── enums/          # Enumerations
│   │   ├── exception/      # Global exception handler
│   │   ├── repository/     # Spring Data repositories
│   │   ├── security/       # JWT, auth filter, user details
│   │   └── service/        # Business logic
│   └── src/main/resources/db/migration/  # Flyway SQL migrations
├── frontend/
│   └── src/app/
│       ├── features/       # Feature modules (auth, dashboard, employees, etc.)
│       ├── guards/         # Auth guards
│       ├── interceptors/   # JWT interceptor
│       └── services/       # API services
└── README.md
```
