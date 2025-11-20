# Service Guidelines � Package-Service Microservice

## 1. Project Overview
- **Purpose**: Manages doctor expertise data, including doctor profiles, specialties, and credential verification workflows.
- **Ecosystem Role**: Registers with Eureka, consumes shared DTOs, and exposes REST APIs consumed by other BookingCare microservices via the API gateway.
- **Technology Stack**: Spring Boot 3.5, Spring Cloud 2025 release train, PostgreSQL, Flyway migrations, Lombok, and Java 21.

## 2. Folder & Package Structure
- `src/main/java/com/bookingcare/expertise`
  - **controller**: REST entry points (e.g., `ExpertiseController`) using `@RestController` & `ResponseEntity`.
  - **service**: Business logic and transaction boundaries (`@Service` + `@Transactional`).
  - **repository**: Spring Data JPA interfaces (`JpaRepository`) with derived queries and soft-delete filters.
  - **entity**: JPA entities with Lombok builders, auditing, and soft-delete annotations (`@SQLDelete`, `@SQLRestriction`).
  - **dto**: Request/response payloads implemented as Java `record`s to ensure immutability and validation annotations.
  - **mapper**: Manual mapping component (`ExpertiseMapper`) that converts between entities and DTOs and encapsulates slug generation.
  - **exception**: Custom domain exceptions (`ApiException`, `ErrorCode`) and centralized error handling (`GlobalExceptionHandler`).
  - **security/client**: Placeholders for security and outbound clients when integration is required.
- `src/main/resources`
  - `application.yml`: Minimal bootstrap; delegates environment-specific config to Spring Cloud Config.
  - `db/migration`: Flyway SQL migration scripts versioned as `V*__description.sql`.

## 3. Dependency Management
- **Maven** with Spring Boot parent POM simplifies plugin/version management.
- Key dependencies:
  - `spring-boot-starter-web`: REST MVC stack.
  - `spring-boot-starter-data-jpa` + `postgresql`: Persistence layer.
  - `spring-boot-starter-validation`: Bean validation for DTO records.
  - `spring-cloud-starter-*` (config, Eureka, OpenFeign): Service discovery and remote calls.
  - `flyway-core` + database-specific module: Controlled schema migrations.
  - `shared-dto` (internal): Ensures contract consistency across services.
- Compiler plugin configures Lombok annotation processing; Spring Boot plugin packages executable jars while excluding Lombok.

## 4. Coding Conventions
- **Classes & Packages**: `PascalCase` classes inside feature-based packages (`com.bookingcare.expertise.service`).
- **Methods & Fields**: `camelCase`; constants (when present) should use `UPPER_SNAKE_CASE`.
- **DTOs**: Java records named `SomethingRequestDTO` / `SomethingResponseDTO` matching REST payload intent.
- **Annotations**: Align with Spring stereotypes (`@RestController`, `@Service`, `@Repository`) and JPA metadata on entities.
- **Validation**: Use Jakarta validation annotations directly on record components or entity fields (e.g., `@NotBlank`, `@Size`).
- **Slug/ID handling**: Helper methods convert strings to UUIDs and normalize text in mapper/service layers.

## 5. Design Patterns & Architectural Conventions
- **Controller ? Service ? Repository** layering separates HTTP concerns, domain logic, and persistence.
- **DTO Pattern**: Records decouple API contracts from entities; `ExpertiseMapper` centralizes conversion logic.
- **Soft Delete**: Entities rely on `@SQLDelete` and boolean flags; repositories expose derived queries like `findByIdAndDeletedFalse`.
- **Builder & Lombok**: Entities leverage `@Builder` & Lombok accessors to reduce boilerplate while staying immutable-friendly.
- **Transactional Boundaries**: Service methods annotated with `@Transactional` (read-only when appropriate) ensure consistency across repository calls.

## 6. Configuration & Environment Management
- Local `application.yml` only sets service name and Config Server import; real settings live in Config Server (`config-server/.../expertise-service.yml`).
- Environment properties include datasource credentials, JPA settings (`ddl-auto`, dialect), Flyway behavior, and Eureka registration.
- Recommend profile-specific overrides via Config Server (e.g., `expertise-service-dev.yml`, `expertise-service-prod.yml`) to keep binaries environment-agnostic.

## 7. Logging & Error Handling
- `GlobalExceptionHandler` (`@RestControllerAdvice`) standardizes responses:
  - Logs errors with `Slf4j` and returns structured payloads (timestamp, status, message, optional validation map).
  - Handles domain `ApiException` with extended metadata (`ErrorCode`).
- Services should log domain-relevant events using `Slf4j` (pattern to expand; currently limited to exception handler).
- Prefer wrapping recoverable issues in `ApiException` coupled with `ErrorCode` enums for consistent HTTP statuses.

## 8. Testing Practices
- Current module ships with a `@SpringBootTest` context smoke test (`ExpertiseApplicationTests`).
- Recommended to extend with focused unit tests (e.g., mapper/service) and Spring MVC slice tests for controllers.
- Use naming pattern `*Test` with JUnit 5; keep tests under matching package structure.

## 9. API Design Patterns
- REST endpoints grouped by domain (`/doctors`, `/specialties`, `/credential-types`) plus internal/admin routes.
- Controllers return `ResponseEntity<>`, allowing explicit status control (e.g., `HttpStatus.SC_CREATED`).
- Path variables use human-readable slugs or UUID strings; services resolve them (`resolveDoctor`).
- DTO records plus Jakarta validation guarantee request correctness; `@Valid` applied in controller parameters.
- Internal/admin APIs exposed under `/internal` and `/admin` prefixes to signal access control expectations.

## 10. Best Practices for Future Services
- Maintain layered architecture and DTO separation to avoid coupling web and persistence models.
- Enforce soft-delete conventions consistently when entities require historical tracking.
- Centralize mapping logic in dedicated components or MapStruct mappers to reduce duplication.
- Push configuration to Config Server, keeping `application.yml` minimal and profiles centrally managed.
- Expand logging beyond exception handling�log key lifecycle events (creation, approval, rejection) with relevant identifiers.
- Strengthen testing coverage: mapper tests, repository slice tests with Testcontainers for PostgreSQL, and contract tests for Feign clients.
- Document API contracts (OpenAPI/Swagger) and align shared DTO versions to prevent drift across microservices.
- Establish coding checklists: `@Transactional` on write operations, validation annotations on DTOs, `Optional` usage for repository queries, and explicit handling of slug/UUID parsing failures.
