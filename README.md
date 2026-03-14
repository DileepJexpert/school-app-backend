# school-app-backend

Spring Boot 3.4.5 + MongoDB multi-tenant backend for school management.

---

## Tech Stack
- **Java 17** + Spring Boot 3.4.5
- **MongoDB** (multi-tenant: each school gets its own database `{tenantId}_db`)
- **Spring Security** + **JJWT 0.12.6** (stateless JWT authentication)
- **Lombok**, Spring Validation, Spring Data MongoDB

---

## Quick Start

```bash
# 1. Set MongoDB URI in application.properties
spring.data.mongodb.uri=mongodb://localhost:27017

# 2. Run
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

On first startup, a default **SUPER_ADMIN** user is automatically created (see credentials below).

---

## Authentication & Login — Testing Guide

### Architecture Overview

| Login Type | Endpoint | Database Used |
|------------|----------|---------------|
| Super Admin | `POST /platform/auth/login` | `platform_db` |
| School users (Admin, Teacher, etc.) | `POST /api/auth/login` + `X-Tenant-ID` header | `{tenantId}_db` |

---

### Step 1 — Super Admin Login (Works Immediately After Startup)

No setup needed. These credentials are seeded automatically on first run:

```
Email:    superadmin@platform.com
Password: SuperAdmin@123
```

```bash
curl -X POST http://localhost:8080/platform/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"superadmin@platform.com","password":"SuperAdmin@123"}'
```

Response:
```json
{
  "token": "eyJhbGci...",
  "refreshToken": "eyJhbGci...",
  "userId": "...",
  "email": "superadmin@platform.com",
  "fullName": "Platform Super Admin",
  "role": "SUPER_ADMIN",
  "tenantId": null,
  "permissions": ["*"],
  "expiresIn": 86400
}
```

> **Important:** Change the default password immediately in production using `POST /api/users/change-password`.

---

### Step 2 — Register a School (Super Admin Required)

Use the token from Step 1:

```bash
curl -X POST http://localhost:8080/platform/schools \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <SUPER_ADMIN_TOKEN>" \
  -d '{
    "tenantId": "springfield",
    "name": "Springfield International Academy",
    "adminEmail": "admin@springfield.com",
    "city": "Springfield",
    "state": "Illinois",
    "board": "CBSE"
  }'
```

> `tenantId` becomes the database name prefix: `springfield_db`. Use only lowercase letters, numbers, underscores, and hyphens.

---

### Step 3 — Create a School Admin User

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <SUPER_ADMIN_TOKEN>" \
  -H "X-Tenant-ID: springfield" \
  -d '{
    "email": "admin@springfield.com",
    "password": "Admin@123",
    "fullName": "School Admin",
    "role": "SCHOOL_ADMIN"
  }'
```

---

### Step 4 — School Admin Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: springfield" \
  -d '{"email":"admin@springfield.com","password":"Admin@123"}'
```

---

### Step 5 — Create Other Role Users (as School Admin)

Replace `role` with any of: `TEACHER`, `ACCOUNTANT`, `TRANSPORT_MANAGER`, `STUDENT`, `PARENT`

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <SCHOOL_ADMIN_TOKEN>" \
  -H "X-Tenant-ID: springfield" \
  -d '{
    "email": "teacher@springfield.com",
    "password": "Teacher@123",
    "fullName": "John Teacher",
    "role": "TEACHER"
  }'
```

---

## Role Reference

| Role | What They Can Do |
|------|-----------------|
| `SUPER_ADMIN` | Full platform access — manage all schools, onboard tenants |
| `SCHOOL_ADMIN` | Full school access — students, fees, results, users, reports |
| `TEACHER` | Attendance (read/write), Results (read/write), Timetable, Students (read) |
| `ACCOUNTANT` | Fees, Expenses, Reports, Students (read) |
| `TRANSPORT_MANAGER` | Buses, Routes, Assignments, Students (read) |
| `STUDENT` | Own attendance, fees, results, timetable (read-only) |
| `PARENT` | Child's attendance, fees, results, timetable (read-only) |

> **Adding a new role:** Add the constant to `UserRole.java` → add case in `AuthService.roleDefaults()` → add `@PreAuthorize` to any new controller methods.

---

## API Endpoints — Auth & Users

| Method | Path | Auth Required | Description |
|--------|------|---------------|-------------|
| POST | `/platform/auth/login` | None | Super Admin login |
| POST | `/api/auth/login` | None + `X-Tenant-ID` | School user login |
| POST | `/api/auth/refresh` | None | Refresh access token |
| POST | `/api/auth/logout` | JWT | Logout (client-side) |
| GET | `/platform/schools/{id}/validate` | None | Validate tenant for Flutter login screen |
| POST | `/api/users` | SCHOOL_ADMIN+ | Create user in tenant |
| GET | `/api/users` | SCHOOL_ADMIN+ | List all users in tenant |
| PUT | `/api/users/{id}` | SCHOOL_ADMIN+ | Update user |
| DELETE | `/api/users/{id}` | SCHOOL_ADMIN+ | Deactivate user |
| POST | `/api/users/change-password` | Any JWT | Change own password |
| POST | `/platform/users` | SUPER_ADMIN | Create another Super Admin |

---

## Multi-Tenant Architecture

Every request (except `/platform/**` and `/api/auth/**`) must include:

```
X-Tenant-ID: springfield
```

This header selects the MongoDB database (`springfield_db`). The JWT token also encodes `tenantId` and takes precedence over the header to prevent tenant-hopping.

---

## JWT Token Details

```json
{
  "sub": "userId",
  "role": "SCHOOL_ADMIN",
  "tenantId": "springfield",
  "name": "School Admin",
  "linkedEntityId": null,
  "iat": 1710000000,
  "exp": 1710086400
}
```

- **Access token** expires in 24 hours (configurable via `app.jwt.expiration`)
- **Refresh token** expires in 7 days (configurable via `app.jwt.refresh-expiration`)
- **Secret key** configurable via `app.jwt.secret` (default key is for dev only — set a strong key in production)

---

## Application Properties Reference

```properties
spring.data.mongodb.uri=mongodb://localhost:27017

# JWT configuration
app.jwt.secret=YourStrongSecretKeyAtLeast32CharactersLong
app.jwt.expiration=86400
app.jwt.refresh-expiration=604800
```
