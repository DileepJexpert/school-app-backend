# Multi-Tenant Configuration — How It Works & How to Configure

---

## What Was Changed (Our Work)

We only added **logging** to existing pieces — no architectural changes were made. The multi-tenant system was already fully built. Here's what we touched:

- `TenantAwareMongoDatabaseFactory` — added a `log.debug` line showing which DB was resolved per request
- `JwtAuthFilter` — added `log.debug` lines showing when `TenantContext` is set from the JWT token
- `DataInitializer` — fixed the duplicate constructor (kept `@Qualifier("platformMongoTemplate")` in the explicit constructor, removed `@RequiredArgsConstructor`)

---

## How the Multi-Tenant System Works (Full Picture)

The system uses **database-level isolation** — each school gets its own MongoDB database. There is no shared collection between tenants.

```
springfield school  →  springfield_db
dps_rohini school   →  dps_rohini_db
Platform registry   →  platform_db   (school list, super admins)
```

There are **5 moving parts** that work together:

---

### Part 1 — `TenantContext` (per-request thread memory)

A simple `ThreadLocal<String>` that holds the current tenant ID for the life of one HTTP request thread. It's set at the start of each request and must be cleared at the end to prevent thread-pool leaks.

---

### Part 2 — `TenantAwareMongoDatabaseFactory` (DB selector)

Extends Spring's `SimpleMongoClientDatabaseFactory`. Every time Spring Data or `MongoTemplate` needs a database connection, it calls `getMongoDatabase()`. This override reads from `TenantContext` and returns the right database:

- `TenantContext` has `"springfield"` → returns `springfield_db`
- `TenantContext` is empty → returns `platform_db` (safe fallback)

---

### Part 3 — `MultiTenantMongoConfig` (Spring wiring)

Defines three beans:

| Bean | What it is | Used by |
|------|-----------|---------|
| `MongoClient` | Single connection pool shared across all tenant DBs | Everything |
| `mongoTemplate` (Primary) | Uses `TenantAwareMongoDatabaseFactory` — tenant-aware | All repositories, services |
| `platformMongoTemplate` | Always points to `platform_db` — ignores tenant | `SchoolOnboardingService`, `DataInitializer`, `UserService` (for SUPER_ADMIN) |

The `@Primary` on the tenant-aware `MongoTemplate` means all Spring Data repositories (`StudentRepository`, `FeeRepository`, etc.) automatically use the correct tenant database **with zero code changes to those repositories**.

---

### Part 4 — `JwtAuthFilter` (tenant resolver per request)

This is where the tenant is set at the beginning of each request. When a valid JWT arrives:

1. Extracts `tenantId` claim from the token
2. Calls `TenantContext.setTenant(tenantId)`
3. From that point, every MongoDB call in the request thread hits the right tenant DB

The JWT is the **trusted source** of tenant identity. The `X-Tenant-ID` header from the client is used only for the login endpoint (before a JWT exists). Once logged in, the JWT overrides the header — this prevents a logged-in user from switching to another school's database by changing the header.

---

### Part 5 — `MongoConfig` (BigDecimal converter)

Not related to multi-tenancy directly. Registers custom converters so `BigDecimal` fields (like fee amounts) are stored as strings in MongoDB rather than failing with a Java 17+ reflection error.

---

## Configuration Required in `application.properties`

Only **one property** is needed:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017
```

The database name is **not set here** — the factory picks it dynamically per request. You do NOT need `spring.data.mongodb.database`.

For MongoDB Atlas:
```properties
spring.data.mongodb.uri=mongodb+srv://<user>:<password>@<cluster>.mongodb.net
```

---

## How a New School Gets Its Database

Databases are **not pre-created**. MongoDB creates a database automatically the first time a document is written to it. So when you register a new school with `tenantId = "greenwood"` and the first user logs in, MongoDB silently creates `greenwood_db`. No manual DB setup needed.

---

## Where to Use `@Qualifier("platformMongoTemplate")`

Any class that needs to read/write **platform-level data** (school registry, super admins) must inject `platformMongoTemplate` explicitly. Otherwise it would try to use the current tenant's DB, which may be null or wrong.

Current usages:
- `DataInitializer` — seeds the default SUPER_ADMIN into `platform_db`
- `UserService` — creates SUPER_ADMIN users and does cross-DB password changes
- `SchoolOnboardingService` — manages the school registry in `platform_db`
- `AuthService` — looks up SUPER_ADMIN users in `platform_db` during platform login
