# SmartQueue

SmartQueue is a queue management system for businesses that provide in-person services, such as clinics, banks, salons, and service centers.

The project is being built as a Java backend modular monolith, organized by feature. Its current implementation covers the application foundation in Phase 0.

## Current Status

Implemented:

- Spring Boot application with a Maven Wrapper.
- PostgreSQL running through Docker Compose.
- Development configuration using environment variables.
- Flyway migration for the initial `users` table.
- Shared success and error response DTOs.
- Global handling for resource-not-found and request-validation errors.
- Public health endpoint and basic endpoint access rules.
- 13 tests covering exception handling, validation, health/security, and application context loading.

Authentication endpoints, JWT, tenant isolation, and queue operations are planned for subsequent phases. The registration request DTO is currently exercised through test-only controllers; a registration API has not been implemented yet.

## Tech Stack

Java 21+, Spring Boot 4.1.1, Maven Wrapper, PostgreSQL 17, Spring Data JPA/Hibernate, Flyway, Jakarta Validation, Spring Security, JUnit, and MockMvc.

## Project Structure

```text
smartqueue/
├── README.md
└── backend/
    └── smartqueue/       # Spring Boot Maven project
        ├── README.md    # Backend setup and testing guide
        ├── .env.example
        ├── compose.yaml
        ├── pom.xml
        └── src/
```

Frontend implementation is planned. Empty local directories are not included in Git.

## Getting Started

See the [backend guide](backend/smartqueue/README.md) for prerequisites, local setup, environment variables, running tests, health checks, and troubleshooting.

All Maven and Docker Compose commands must be run from `backend/smartqueue`.
