# BookingCare Service Blueprint (Template Rollout)

This document standardizes the structure, tooling, and operational practices for new BookingCare microservices. Use it to replicate the existing `account-service` patterns across future services while addressing current gaps.

---

## 1. Current Architecture Snapshot (Account Service)

- **Entry Points**: `AuthController`, `UserController`, `AccountManagementController` expose REST endpoints under `/api/v1/account/**`.
- **Modules**:
  - `service/` layer (`AuthService`, `UserService`, `AccountManagementService`, `JwtService`) encapsulates business logic.
  - `repository/` (Spring Data JPA) manages persistence against PostgreSQL tables defined via Flyway migrations.
  - `dto/` + `mapper/UsersMapper` bridge HTTP payloads and entities.
  - `security/` configures Spring Security filters relying on gateway propagated headers.
  - `exception/` implements global error handling (currently using `RuntimeException` fallbacks).
- **Data Access**: Entities (`Accounts`, `Users`, `Roles`, `RefreshToken`) implement soft-delete via `isDeleted` fields; `V1__init_database.sql` bootstraps schema. Repository generics still use `Integer` while IDs are `String` UUIDs.
- **Cross-Cutting**:
  - **Auth**: JWT issuance with `jjwt` using shared secret; passwords hashed via BCrypt; trust boundary assumes API Gateway injects `X-User-Name`, `X-User-Roles`.
  - **Config**: Spring Config Server via `application.yml` (`spring.config.import`), plus environment overrides.
  - **Logging**: `Slf4j` with Spring Boot defaults; security/web logging set to DEBUG.
  - **Errors**: `GlobalExceptionHandler` catches `RuntimeException` (500) and validation errors (400), but lacks `ApiException` mapping.

---

## 2. Standardized Project Skeleton

```
service-template/
├─ .editorconfig
├─ .gitignore
├─ Dockerfile
├─ Makefile
├─ README.md
├─ docker-compose.yml
├─ openapi.yaml
├─ pom.xml
├─ scripts/
│  ├─ migrate.sh
│  └─ seed.sh
├─ src/
│  ├─ main/
│  │  ├─ java/com/<org>/<service>/
│  │  │  ├─ ServiceApplication.java
│  │  │  ├─ config/
│  │  │  │  ├─ AppConfig.java
│  │  │  │  └─ OpenApiConfig.java
│  │  │  ├─ security/
│  │  │  │  ├─ SecurityConfig.java
│  │  │  │  └─ filter/GatewayAuthFilter.java
│  │  │  ├─ controller/
│  │  │  │  └─ HealthController.java
│  │  │  ├─ dto/
│  │  │  ├─ entity/
│  │  │  ├─ exception/
│  │  │  ├─ mapper/
│  │  │  ├─ repository/
│  │  │  ├─ service/
│  │  │  └─ util/
│  │  └─ resources/
│  │     ├─ application.yml
│  │     ├─ application-local.yml
│  │     ├─ db/migration/V1__init.sql
│  │     └─ static/placeholder.txt
│  └─ test/java/com/<org>/<service>/
│     ├─ controller/
│     ├─ service/
│     └─ integration/
└─ templates/postman_collection.json
```

> Replace `<org>` with `bookingcare` and `<service>` with the service key (for example, `schedule`).

---

## 3. Mandatory File Templates

```ini
; .editorconfig
root = true
[*]
charset = utf-8
end_of_line = lf
indent_style = space
indent_size = 4
insert_final_newline = true
trim_trailing_whitespace = true
```

```gitignore
# .gitignore
target/
.idea/
*.iml
*.log
.env
application-local.yml
```

```makefile
# Makefile
ENV ?= local
APP_NAME := bookingcare-<service>

.PHONY: boot test lint build docker migrate seed

boot:
	mvn spring-boot:run -Dspring-boot.run.profiles=$(ENV)

test:
	mvn test

lint:
	mvn -DskipTests=true verify

build:
	mvn clean package -DskipTests

docker:
	docker build -t $(APP_NAME):latest .

migrate:
	sh ./scripts/migrate.sh $(ENV)

seed:
	sh ./scripts/seed.sh $(ENV)
```

```dockerfile
# Dockerfile
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java ${JAVA_OPTS} -jar app.jar"]
```

```yaml
# docker-compose.yml
version: "3.9"
services:
  service:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: local
      SPRING_CONFIG_IMPORT: optional:configserver:http://config-server:8888
    depends_on:
      - postgres
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: bookingcare
      POSTGRES_USER: bookingcare
      POSTGRES_PASSWORD: bookingcare
    ports:
      - "5432:5432"
```

```markdown
<!-- README.md -->
# <Service Name>

## Overview
Short summary of responsibilities and upstream/downstream dependencies.

## Quick Start
```bash
make boot
```

## Configuration
- Config Server: `SPRING_CONFIG_IMPORT`
- Local overrides: `src/main/resources/application-local.yml` (gitignored).

## Database
```bash
make migrate
make seed
```

## Testing
```bash
make test
```

## Observability
- Health: `GET /actuator/health`
- Readiness: `GET /actuator/health/readiness`
- Metrics: `GET /actuator/prometheus`
- Logs: JSON to stdout (structured).

## Deployment
1. `make build`
2. `make docker`
3. Push image via CI pipeline.
```

```yaml
# application.yml (excerpt)
spring:
  application:
    name: <service>
  config:
    import: optional:configserver:http://config-server:8888
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      probes:
        enabled: true
jwt:
  secret: ${JWT_SECRET:change-me}
  expiration: 86400000
  refresh-token:
    expiration: 604800000
logging:
  level:
    root: INFO
    org.springframework.security: INFO
```

```java
// src/main/java/com/<org>/<service>/controller/HealthController.java
@RestController
@RequestMapping("/actuator")
public class HealthController {

    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        return ResponseEntity.ok(Map.of("status", "READY"));
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> live() {
        return ResponseEntity.ok(Map.of("status", "ALIVE"));
    }
}
```

```yaml
# openapi.yaml (stub)
openapi: 3.0.3
info:
  title: BookingCare <Service> API
  version: 1.0.0
servers:
  - url: https://api.bookingcare.vn
paths:
  /api/v1/<resource>:
    get:
      summary: List resources
      responses:
        '200':
          description: OK
components:
  schemas:
    <ResourceResponse>:
      type: object
      properties:
        id:
          type: string
```

```bash
# scripts/migrate.sh
#!/usr/bin/env bash
set -euo pipefail
PROFILE=${1:-local}
mvn -q -DskipTests flyway:migrate -Dspring-boot.run.profiles=$PROFILE
```

```bash
# scripts/seed.sh
#!/usr/bin/env bash
set -euo pipefail
psql "$DATABASE_URL" -f ./scripts/seed.sql
```

```yaml
# .github/workflows/ci.yml
name: CI
on:
  push:
    branches: [ main ]
  pull_request: {}
jobs:
  build-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
      - run: mvn -B verify
  docker:
    needs: build-test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: docker/setup-buildx-action@v3
      - run: docker build -t ghcr.io/<org>/<service>:${{ github.sha }} .
      - run: echo "${{ secrets.GITHUB_TOKEN }}" | docker login ghcr.io -u ${{ github.actor }} --password-stdin
      - run: docker push ghcr.io/<org>/<service>:${{ github.sha }}
  security:
    needs: docker
    runs-on: ubuntu-latest
    steps:
      - uses: aquasecurity/trivy-action@master
        with:
          image-ref: ghcr.io/<org>/<service>:${{ github.sha }}
```

---

## 4. Observability Endpoints

- **Health**: `GET /actuator/health` (Spring Boot default). Ensure readiness (`/actuator/health/readiness`) and liveness (`/actuator/health/liveness`) probes are enabled.
- **Custom Readiness**: `GET /actuator/ready` (via `HealthController` example) if gateway requires.
- **Metrics**: `GET /actuator/prometheus` for Prometheus scraping.
- **Info**: `GET /actuator/info` for build metadata (configure via Maven build properties).
- **Logging**: Configure JSON via `logging.pattern.console` or Logback encoder.

---

## 5. New Service Checklist

1. Duplicate the template tree or run scaffold script.
2. Rename package to `com.bookingcare.<service>`.
3. Update `pom.xml` group/artifact/name.
4. Configure `application.yml` (service name, config import, actuator exposure).
5. Create `application-local.yml` with local DB credentials (gitignored).
6. Write `V1__init.sql` migration for base schema.
7. Implement entities with UUID IDs and `isDeleted` flags.
8. Declare repositories using `JpaRepository<Entity, String>`.
9. Define DTOs with Jakarta validation.
10. Implement mapper (MapStruct optional) for entity ↔ DTO.
11. Build service layer with `@Transactional` methods.
12. Expose controllers under `/api/v1/<resource>`.
13. Configure security: public endpoints, role-based access, gateway filter.
14. Add actuator health/metrics endpoints.
15. Write unit tests (service) and controller slice tests.
16. Seed sample data (SQL) if required.
17. Run `make migrate` and `make test`.
18. Build Docker image `make docker` and verify via `docker-compose up`.
19. Update README sections (overview, run instructions).
20. Commit with Conventional Commits message; open PR; ensure CI (build, test, docker, security) passes.

---

## 6. Prompt Library

1. **CRUD Module**  
   “Generate Spring Boot CRUD module for entity `<Entity>` (fields: `<field:type>`). Use Spring Data JPA, expose REST endpoints under `/api/v1/<entity>`, include DTOs and validation.”

2. **DTO + Service Stack**  
   “Create request/response DTOs, repository, service, controller for feature `<Feature>` using `isDeleted` soft delete semantics. Include Jakarta validation annotations and mapper logic.”

3. **Database Migration**  
   “Produce Flyway migration `V<version>__<name>.sql` to create table `<table>` with columns `<columns>` and foreign keys `<relations>`, using PostgreSQL syntax.”

4. **Tests & Seed Data**  
   “Write JUnit tests for `<Feature>` service (Mockito) and controller (`@WebMvcTest`). Provide SQL seed script inserting baseline rows for integration testing.”

5. **OpenAPI Sync**  
   “Generate OpenAPI 3 paths/components for `<Feature>` endpoints (list/create/get/update/delete) reflecting DTO schemas and validation. Include error responses 400/404/500.”

---

## 7. Gap Analysis & Recommendations

- **Repository Generics**: Align `JpaRepository` interfaces to use `String` IDs to match UUID entities and prevent type mismatch bugs.
- **Exception Strategy**: Replace ad-hoc `RuntimeException` with `ApiException` + `ErrorCode`; update `GlobalExceptionHandler` to emit consistent HTTP statuses.
- **Refresh Tokens**: Either implement token rotation leveraging `RefreshToken` entity or remove until required to avoid dead code.
- **Security Hardening**: Consider verifying JWT signatures locally or introducing gateway signature checks to reduce trust boundary risks.
- **Logging**: Switch to structured JSON logging and reduce DEBUG noise in production profiles.
- **Testing**: Add unit/integration coverage; current service lacks automated tests.
- **Secrets Management**: Move hard-coded JWT secret to Config Server or secret manager; enforce `.env` exclusion.
- **Docker Build**: Standardize on multi-stage Dockerfile (current account-service lacks it).
- **Documentation**: Adopt standardized README/Blueprint for every service; include run, migration, and troubleshooting sections.
- **Observability**: Enable Prometheus metrics and readiness probes consistently across services.

---

Use this blueprint to provision new BookingCare services quickly while ensuring architectural consistency, operational readiness, and improved developer experience.

