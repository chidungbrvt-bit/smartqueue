# SmartQueue Backend

## Overview

SmartQueue is a queue management system for businesses that provide in-person services. This directory contains the Spring Boot Maven backend.

See the [project overview](../../README.md) for the repository structure and implementation status.

## Current Status

The Phase 0 backend foundation includes PostgreSQL configuration, an initial Flyway migration, shared response DTOs, global exception handling, request validation, a health endpoint, and tests.

The current security configuration allows anonymous `GET /api/health` requests and requires authentication for other requests. HTTP Basic is enabled temporarily, form login is disabled, and CSRF protection remains enabled. Authentication endpoints and JWT will be implemented in Phase 1.

`RegisterRequest` validation is currently verified using controllers defined only in test sources. The application does not yet expose a registration endpoint.

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 21+; Maven compilation target: Java 21 |
| Framework | Spring Boot 4.1.1 |
| Build | Maven Wrapper |
| Database | PostgreSQL 17 |
| Persistence | Spring Data JPA / Hibernate |
| Schema migrations | Flyway |
| Validation | Jakarta Validation / Hibernate Validator |
| Security | Spring Security |
| Testing | JUnit, Spring Boot Test, MockMvc |
| Local infrastructure | Docker Desktop / Docker Compose |

## Project Structure

```text
backend/smartqueue/
├── .env.example
├── compose.yaml
├── pom.xml
├── mvnw
├── mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/smartqueue/
    │   │   ├── SmartqueueApplication.java
    │   │   ├── auth/dto/
    │   │   ├── common/exception/
    │   │   ├── common/security/
    │   │   ├── health/controller/
    │   │   └── response/
    │   └── resources/
    │       ├── application.properties
    │       ├── application-dev.yml
    │       └── db/migration/
    └── test/java/com/smartqueue/
```

## Prerequisites

- JDK 21 or later, with `java` available in your terminal.
- Git.
- Docker Desktop with the Linux engine running and Docker Compose available.
- PowerShell on Windows for the commands below.
- Available local ports: `5433` for PostgreSQL and `8080` for the application.

A separate Maven installation or local PostgreSQL installation is not required. The Maven Wrapper downloads its configured Maven distribution; the first run requires internet access to download dependencies.

Check your tools:

```powershell
java -version
git --version
docker version
docker compose version
```

`docker version` must report a working server as well as a client.

## Local Setup

### 1. Clone the repository

```powershell
git clone https://github.com/chidungbrvt-bit/smartqueue.git
Set-Location smartqueue\backend\smartqueue
```

If you already have the repository, open a terminal in its `backend/smartqueue` directory instead. Run the remaining setup and Maven commands from that directory.

### 2. Configure the local environment

Create `.env` from the [example file](.env.example) if it does not already exist:

```powershell
if (-not (Test-Path -LiteralPath .env)) {
    Copy-Item -LiteralPath .env.example -Destination .env
}
```

Open `.env` in your editor. Keep the database and user names below, and replace the password placeholder with your own local development password before starting PostgreSQL for the first time:

```dotenv
POSTGRES_DB=smartqueue
POSTGRES_USER=smartqueue
POSTGRES_PASSWORD=replace_with_your_local_password
```

The current development datasource configuration expects the database and username to be `smartqueue`. The `.env` file is ignored by Git; commit only `.env.example` with placeholder values.

### 3. Start PostgreSQL

Open Docker Desktop and wait for its engine to be running. Then run:

```powershell
docker compose up -d postgres
docker compose ps
```

Wait until the PostgreSQL service reports `healthy`. Repeat `docker compose ps` if it is still starting.

The port mapping is:

```text
127.0.0.1:5433 -> PostgreSQL container:5432
```

The backend connects to `jdbc:postgresql://localhost:5433/smartqueue`. Database files are persisted in the Compose `postgres_data` volume.

### 4. Set the application database password

In the same PowerShell terminal that will run Maven:

```powershell
$smartqueueConfig = docker compose config --format json | ConvertFrom-Json
$env:DB_PASSWORD = $smartqueueConfig.services.postgres.environment.POSTGRES_PASSWORD
```

Docker Compose reads `.env`, but Maven does not automatically load it. These commands transfer the resolved Compose password into the `DB_PASSWORD` variable used by [application-dev.yml](src/main/resources/application-dev.yml), without printing the password.

Repeat this step when opening a new terminal to run the application or database-dependent tests.

### 5. Start the application

```powershell
.\mvnw.cmd "-Dspring-boot.run.profiles=dev" spring-boot:run
```

Wait for `Started SmartqueueApplication`. The HTTP server runs on `http://localhost:8080`.

Flyway applies pending migrations from `src/main/resources/db/migration` during startup. The initial migration creates the `users` table. Hibernate uses `ddl-auto: validate`; schema changes are managed through Flyway.

## Running Tests

Keep PostgreSQL running. In a PowerShell terminal inside `backend/smartqueue`, set the password and run:

```powershell
$smartqueueConfig = docker compose config --format json | ConvertFrom-Json
$env:DB_PASSWORD = $smartqueueConfig.services.postgres.environment.POSTGRES_PASSWORD
.\mvnw.cmd test
```

The current suite contains 13 test methods:

| Test class | Tests | Scope |
|---|---:|---|
| `GlobalExceptionHandlerTest` | 1 | Resource-not-found responses |
| `GlobalExceptionHandlerValidationTest` | 8 | Validation failures, length limits, valid input, and password exclusion from error responses |
| `HealthControllerTest` | 3 | Health responses and anonymous access rules with security filters enabled |
| `SmartqueueApplicationTests` | 1 | Full application context initialization with PostgreSQL and Flyway |

Expected summary:

```text
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

To run only the health/security tests:

```powershell
.\mvnw.cmd "-Dtest=HealthControllerTest" test
```

`SmartqueueApplicationTests` selects the `dev` profile and uses the local development database. It is a startup smoke test, not an isolated database test environment. Its default mock web environment does not occupy port `8080`.

## Health Check

With the application running, open a second terminal:

```powershell
curl.exe -i http://localhost:8080/api/health
```

Expected result: HTTP `200` and a JSON response with this structure:

```json
{
  "message": "Service is running",
  "data": {
    "status": "UP"
  },
  "timestamp": "2026-10-20T00:00:00Z"
}
```

The timestamp changes on each request. This endpoint confirms that the web application responds; it does not check database readiness.

To check that requests outside the public health rule require authentication:

```powershell
curl.exe -i http://localhost:8080/api/private-probe
```

Expected result without credentials: HTTP `401`. This path is an authorization probe, not an implemented business endpoint; the security filter chain rejects the anonymous request before controller lookup.

### Stop the local services

Press `Ctrl+C` in the terminal running Spring Boot. To stop PostgreSQL while retaining its data:

```powershell
docker compose stop postgres
```

## Troubleshooting

| Symptom | What to check |
|---|---|
| Docker cannot connect to `dockerDesktopLinuxEngine` | Open Docker Desktop, wait for the Linux engine to run, and check `docker version` before using Compose. |
| `Connection to localhost:5433 refused` | Check `docker compose ps`; PostgreSQL must be running and healthy with host port `5433` published. |
| `password authentication failed for user "smartqueue"` | Repeat the `DB_PASSWORD` setup in the exact terminal running Maven. Check that `.env` contains the credentials used to initialize the database. |
| Database login still fails after changing `.env` | PostgreSQL initialization variables do not reset an existing database user's password. Reconcile the existing database credentials with your local configuration. |
| A datasource cannot be configured | Start with the `dev` profile, or verify that `@ActiveProfiles("dev")` remains on the context-loading test. |
| Health returns `401` | Use exactly `GET /api/health` and check the matcher in `SecurityConfig`. Other paths require authentication. |
| Port `8080` is already in use | Check for another Spring Boot process or an application already running from IntelliJ. |
| A test reports a migration validation/checksum error | Check changes to existing migration files. Keep applied migrations unchanged and add a new migration for subsequent schema changes. |

See the [official PostgreSQL image documentation](https://hub.docker.com/_/postgres) for details on initialization variables and existing database volumes.

The generated Spring Security development password and Mockito/Java-agent warnings may appear in test output. Check the final Maven test summary to determine whether the build succeeded.
