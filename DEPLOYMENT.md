# Springfield International Academy — Deployment Guide

Complete step-by-step guide for deploying the full stack:
**Flutter Web (Netlify) → Spring Boot API (Koyeb) → MongoDB (Atlas)**

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Prerequisites](#2-prerequisites)
3. [MongoDB Atlas Setup](#3-mongodb-atlas-setup)
4. [Backend — Spring Boot on Koyeb](#4-backend--spring-boot-on-koyeb)
5. [Frontend — Flutter Web on Netlify](#5-frontend--flutter-web-on-netlify)
6. [Local Development Setup](#6-local-development-setup)
7. [Environment Variables Reference](#7-environment-variables-reference)
8. [Backup & Restore](#8-backup--restore)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         PRODUCTION STACK                        │
│                                                                 │
│  ┌──────────────┐    HTTPS     ┌──────────────┐   SRV/TLS      │
│  │   Flutter    │ ──────────►  │  Spring Boot │ ────────────►  │
│  │   Web App    │              │   REST API   │                 │
│  │  (Netlify)   │ ◄──────────  │   (Koyeb)   │  ┌───────────┐  │
│  └──────────────┘   JSON/ZIP   └──────────────┘  │  MongoDB  │  │
│         │                            │           │   Atlas   │  │
│  URL: netlify.app            URL: koyeb.app      └───────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

Repos:
  UI      → github.com/DileepJexpert/school-project-ui
  Backend → github.com/DileepJexpert/school-app-backend
```

| Layer | Technology | Hosting | Port |
|---|---|---|---|
| Frontend | Flutter 3.x Web | Netlify | 443 (HTTPS) |
| Backend | Spring Boot 3.4.5 + Java 17 | Koyeb (Docker) | 8080 → 443 |
| Database | MongoDB (Atlas cloud) | AWS/GCP (Atlas) | 27017 (SRV) |

---

## 2. Prerequisites

### Tools to install on your machine

| Tool | Version | Install |
|---|---|---|
| Java JDK | 17+ | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org |
| Flutter SDK | 3.x | https://flutter.dev/docs/get-started/install |
| Docker Desktop | Latest | https://docker.com/products/docker-desktop |
| Git | Latest | https://git-scm.com |

### Accounts you need

| Service | Free Tier | URL |
|---|---|---|
| MongoDB Atlas | 512 MB free (M0) | https://cloud.mongodb.com |
| Koyeb | 1 free web service | https://koyeb.com |
| Netlify | Unlimited free static sites | https://netlify.com |
| GitHub | Free | https://github.com |

---

## 3. MongoDB Atlas Setup

### Step 1 — Create a free cluster

1. Go to https://cloud.mongodb.com and sign in / sign up
2. Click **"Build a Database"** → choose **M0 (Free Tier)**
3. Cloud Provider: **AWS** → Region closest to you
4. Cluster Name: `school-cluster` → Click **Create**

### Step 2 — Create a database user

1. Left sidebar → **Database Access** → **Add New Database User**
2. Authentication: **Password**
3. Username: `dileep`
4. Password: (create a strong password — save it)
5. Built-in Role: **Atlas admin** (or "Read and write to any database")
6. Click **Add User**

### Step 3 — Whitelist IP addresses

1. Left sidebar → **Network Access** → **Add IP Address**
2. For development: click **"Add Current IP Address"**
3. For Koyeb (production): click **"Allow Access from Anywhere"** → `0.0.0.0/0`
   > Note: Atlas still requires credentials, so this is safe.
4. Click **Confirm**

### Step 4 — Get the connection string

1. Left sidebar → **Database** → **Connect** on your cluster
2. Choose **"Drivers"** → Driver: **Java** → Version: **4.3 or later**
3. Copy the connection string. It looks like:
   ```
   mongodb+srv://dileep:<password>@cluster0.z9llhfs.mongodb.net/?retryWrites=true&w=majority&appName=school-cluster
   ```
4. Replace `<password>` with your actual password
5. Append the database name before `?`:
   ```
   mongodb+srv://dileep:YOUR_PASSWORD@cluster0.z9llhfs.mongodb.net/school-db?retryWrites=true&w=majority&appName=school-cluster
   ```

### Step 5 — Verify Atlas is working (optional)

```bash
# Install MongoDB Shell (mongosh) then run:
mongosh "mongodb+srv://dileep:YOUR_PASSWORD@cluster0.z9llhfs.mongodb.net/school-db"
# Should connect and show: Atlas atlas-xxxxx-shard-0 [primary] school-db>
```

---

## 4. Backend — Spring Boot on Koyeb

### Step 1 — Configure application.properties

File: `src/main/resources/application.properties`

```properties
# MongoDB Atlas URI — use environment variable in production
spring.data.mongodb.uri=${MONGODB_URI:mongodb+srv://dileep:YOUR_PASSWORD@cluster0.z9llhfs.mongodb.net/school-db?retryWrites=true&w=majority&appName=school-cluster}

spring.data.mongodb.lazy-initialization=true

# Koyeb injects PORT automatically
server.port=${PORT:8080}
server.address=0.0.0.0
```

> **Security**: Never hardcode the real password in the file committed to GitHub.
> Use environment variables on Koyeb (see Step 4).

### Step 2 — Test the build locally

```bash
cd school-app-backend

# Run tests
./mvnw test

# Build the JAR
./mvnw clean package -DskipTests

# Run locally (uses Atlas URI from properties)
java -jar target/schoolApp-0.0.1-SNAPSHOT.jar
```

Open http://localhost:8080/api/students — should return `[]` or student data.

### Step 3 — The Dockerfile (already present)

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-Xmx350m", "-Xms128m", "-jar", "app.jar"]
```

> `-Xmx350m` keeps memory within Koyeb's free tier limit (512 MB).

### Step 4 — Deploy to Koyeb

1. Go to https://koyeb.com → Sign in → **Create Service**
2. Source: **GitHub** → connect your GitHub account → select `school-app-backend`
3. Branch: `main`
4. Builder: **Dockerfile** (Koyeb auto-detects the Dockerfile)
5. **Instance type**: Free (Nano — 0.1 vCPU, 512 MB RAM)
6. **Environment Variables** — click "Add Variable":

   | Key | Value |
   |---|---|
   | `MONGODB_URI` | `mongodb+srv://dileep:YOUR_PASSWORD@cluster0.z9llhfs.mongodb.net/school-db?retryWrites=true&w=majority&appName=school-cluster` |

7. **Port**: `8080` (Koyeb maps this to HTTPS automatically)
8. Click **Deploy**

### Step 5 — Get your Koyeb URL

After deployment (2–5 min), Koyeb assigns a URL like:
```
https://lazy-gena-schoolapp-1214a249.koyeb.app
```

Test it:
```
https://lazy-gena-schoolapp-1214a249.koyeb.app/api/students
```

Should return a JSON array.

### Step 6 — Verify all endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/students` | List all students |
| GET | `/api/fees` | Fee structures |
| GET | `/api/attendance` | Attendance records |
| GET | `/api/expenses` | Expense records |
| GET | `/api/results` | Student results |
| GET | `/api/timetable` | Timetable |
| GET | `/api/transport` | Transport routes |
| GET | `/api/admin/backup` | Download full backup ZIP |

---

## 5. Frontend — Flutter Web on Netlify

### Step 1 — Configure the API URL

The API base URL is injected at **build time** via `--dart-define`.

File: `lib/services/dio_client.dart` reads it as:
```dart
const String apiUrl = String.fromEnvironment('API_BASE_URL',
    defaultValue: 'http://localhost:8080/api');
```

### Step 2 — The netlify.toml (already present)

```toml
[build]
  command = "flutter build web --release --dart-define=API_BASE_URL=https://lazy-gena-schoolapp-1214a249.koyeb.app/api"
  publish = "build/web"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

> The `[[redirects]]` rule is critical — it makes Flutter's client-side router work
> correctly on page refresh and direct URL access.

### Step 3 — Deploy to Netlify

#### Option A — Connect GitHub (recommended, auto-deploys on push)

1. Go to https://netlify.com → **Add new site** → **Import an existing project**
2. Connect GitHub → select `school-project-ui`
3. Branch: `main`
4. Build command: (auto-read from `netlify.toml`)
5. Publish directory: `build/web`
6. Click **Deploy site**

Netlify installs Flutter, builds, and deploys automatically.
Every `git push` to `main` triggers a new deploy.

#### Option B — Manual deploy (quick test)

```bash
cd school-project-ui

# Build
flutter build web --release \
  --dart-define=API_BASE_URL=https://lazy-gena-schoolapp-1214a249.koyeb.app/api

# Deploy with Netlify CLI
npm install -g netlify-cli
netlify deploy --dir=build/web --prod
```

### Step 4 — Verify deployment

1. Open your Netlify URL (e.g. `https://springfield-academy.netlify.app`)
2. Navigate to any page — all routes should work including on refresh
3. Go to the Admin Panel → Settings → click **Backup Now** — should download a ZIP

### Step 5 — Custom domain (optional)

1. Netlify → Site settings → **Domain management** → **Add custom domain**
2. Enter your domain, e.g. `www.springfieldacademy.in`
3. Update DNS at your registrar: add CNAME → `springfield-academy.netlify.app`
4. Netlify auto-provisions a free SSL certificate (Let's Encrypt)

---

## 6. Local Development Setup

### Run everything locally

#### Terminal 1 — MongoDB (Docker)

```bash
cd school-app-backend
docker-compose up -d

# MongoDB available at: mongodb://root:root@localhost:27017
# Mongo Express UI at:  http://localhost:8081
```

#### Terminal 2 — Spring Boot backend

```bash
cd school-app-backend

# Use local MongoDB (edit application.properties or set env var)
MONGODB_URI="mongodb://root:root@localhost:27017/school-db?authSource=admin" \
  ./mvnw spring-boot:run
```

Or to use Atlas locally:
```bash
./mvnw spring-boot:run
# (uses the Atlas URI already in application.properties)
```

Backend available at: http://localhost:8080/api

#### Terminal 3 — Flutter Web frontend

```bash
cd school-project-ui
flutter run -d chrome \
  --dart-define=API_BASE_URL=http://localhost:8080/api
```

Frontend available at: http://localhost:PORT (Flutter prints the exact URL)

---

## 7. Environment Variables Reference

### Backend (Koyeb)

| Variable | Required | Example | Description |
|---|---|---|---|
| `MONGODB_URI` | Yes | `mongodb+srv://...` | Full Atlas connection string |
| `PORT` | No | `8080` | Koyeb injects this automatically |

### Frontend (build-time `--dart-define`)

| Variable | Required | Default | Description |
|---|---|---|---|
| `API_BASE_URL` | Yes | `http://localhost:8080/api` | Full URL to the Spring Boot API |

---

## 8. Backup & Restore

### Download a backup

In the admin panel:
1. Login → **Settings** → **Data Backup** → **Backup Now**
2. A file `school_backup_YYYY-MM-DD_HH-mm.zip` downloads to your browser's download folder

Or call the API directly:
```bash
curl -o backup.zip \
  https://lazy-gena-schoolapp-1214a249.koyeb.app/api/admin/backup
```

### What's inside the ZIP

```
school_backup_2026-03-08_14-30.zip
├── students.json
├── fee_profiles.json
├── fee_structures.json
├── payment_records.json
├── expenses.json
├── attendance.json
├── results.json
├── timetable.json
├── transport_routes.json
├── buses.json
└── notifications.json
```

Each file is a pretty-printed JSON array of all documents in that MongoDB collection,
including MongoDB `ObjectId` fields in the `{ "$oid": "..." }` format.

### Restore to MongoDB

```bash
# 1. Unzip
unzip school_backup_2026-03-08_14-30.zip -d school_backup/
cd school_backup/

# 2. Import each file (local MongoDB)
for file in *.json; do
  collection="${file%.json}"
  mongoimport \
    --uri "mongodb://root:root@localhost:27017/school-db?authSource=admin" \
    --collection "$collection" \
    --file "$file" \
    --jsonArray
  echo "Imported $collection"
done

# 3. Or restore to Atlas
for file in *.json; do
  collection="${file%.json}"
  mongoimport \
    --uri "mongodb+srv://dileep:YOUR_PASSWORD@cluster0.z9llhfs.mongodb.net/school-db" \
    --collection "$collection" \
    --file "$file" \
    --jsonArray
  echo "Imported $collection"
done
```

> After restore, start the Spring Boot app once — it will recreate all MongoDB indexes
> automatically via `@Indexed` annotations.

### Backup notes

| Feature | Status |
|---|---|
| All student records | Restored |
| All financial records | Restored |
| MongoDB ObjectIds preserved | Yes |
| Works with Atlas | Yes |
| Indexes recreated | Yes (auto, on app startup) |
| Passwords / secrets | Not stored in DB — use env vars |

---

## 9. Troubleshooting

### Backend won't start — MongoDB connection refused

**Symptom**: `com.mongodb.MongoSocketOpenException: Exception opening socket`

**Fix**: Check the `MONGODB_URI` environment variable on Koyeb.
Make sure the Atlas cluster is running and the IP `0.0.0.0/0` is whitelisted.

---

### Koyeb deploy fails — out of memory

**Symptom**: Container exits with OOMKilled during Maven build

**Fix**: The Dockerfile uses two stages — the build stage uses Maven (1 GB+) and
the run stage is lightweight. Make sure you haven't changed the two-stage Dockerfile.
Koyeb's free tier build runner has more memory than the runtime instance.

---

### Flutter build fails — `API_BASE_URL` not set

**Symptom**: App connects to `localhost:8080` in production

**Fix**: Ensure `netlify.toml` has the correct `--dart-define` flag:
```toml
command = "flutter build web --release --dart-define=API_BASE_URL=https://YOUR-APP.koyeb.app/api"
```

---

### Flutter page refresh gives 404 on Netlify

**Symptom**: Navigating directly to `/admin` or `/results` gives a 404

**Fix**: Ensure `netlify.toml` has the redirect rule:
```toml
[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

---

### CORS error in browser console

**Symptom**: `Access to XMLHttpRequest blocked by CORS policy`

**Fix**: The backend `WebConfig.java` allows all origins (`*`).
If you added Spring Security, ensure the security filter chain calls `.cors()` before `.csrf()`.

---

### Admin backup download does nothing

**Symptom**: Clicking "Backup Now" shows no download

**Fix**:
1. Check the browser console for errors
2. Check that the Koyeb service is running (not sleeping)
3. Try the direct curl command:
   ```bash
   curl -v https://YOUR-APP.koyeb.app/api/admin/backup -o test.zip
   ```
4. Koyeb free tier services sleep after inactivity — the first request may take 10–30 s to wake up.

---

### Koyeb service sleeping (cold start)

Koyeb's free tier pauses services after ~10 minutes of inactivity.
The first request after a pause takes 10–30 seconds.

**To reduce cold starts**: Keep the service warm by pinging it with a cron job (e.g. via UptimeRobot — free tier pings your URL every 5 minutes):
1. Go to https://uptimerobot.com → **Add New Monitor**
2. Type: **HTTP(s)**
3. URL: `https://lazy-gena-schoolapp-1214a249.koyeb.app/api/students`
4. Monitoring Interval: **5 minutes**

---

## Quick Reference — Deploy Checklist

```
MongoDB Atlas
  [ ] Cluster created (M0 free)
  [ ] Database user created (username + password saved)
  [ ] IP 0.0.0.0/0 whitelisted
  [ ] Connection string tested locally

Backend (Koyeb)
  [ ] GitHub repo connected
  [ ] Dockerfile present at repo root
  [ ] MONGODB_URI env var set on Koyeb
  [ ] Service deployed and green
  [ ] /api/students returns JSON

Frontend (Netlify)
  [ ] GitHub repo connected
  [ ] netlify.toml has correct API_BASE_URL
  [ ] netlify.toml has [[redirects]] rule
  [ ] Site deployed and loads
  [ ] Admin panel login works
  [ ] Backup Now downloads a ZIP
```

---

*Last updated: 2026-03-08 | Stack: Flutter 3.x + Spring Boot 3.4.5 + MongoDB Atlas*
